package org.nutz.walnut.ext.net.mailx;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.net.mailx.bean.MailxConfig;
import org.nutz.walnut.ext.net.mailx.bean.WnMail;
import org.nutz.walnut.ext.net.mailx.impl.WnMailPosting;
import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wlog;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

public class cmd_mailx extends JvmFilterExecutor<MailxContext, MailxFilter> {

    private static final Log log = Wlog.getEXT();

    public cmd_mailx() {
        super(MailxContext.class, MailxFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(quiet|ajax|json)$");
    }

    @Override
    protected MailxContext newContext() {
        return new MailxContext();
    }

    @Override
    protected void prepare(WnSystem sys, MailxContext fc) {
        // 读取配置文件
        String ph = fc.params.val(0, "default");
        if (!ph.endsWith(".json")) {
            ph += ".json";
        }
        String aph = Wn.appendPath("~/.mailx", ph);
        WnObj oConf = Wn.checkObj(sys, aph);
        String json = sys.io.readText(oConf);
        fc.config = Json.fromJson(MailxConfig.class, json);

        // 准备 Email 构造器
        fc.mail = new WnMail();
        fc.vars = new NutMap();

        // 更多上下文设置
        if (fc.params.has("lang")) {
            fc.lang = fc.params.getString("lang", fc.config.smtp.getLang());
        }

    }

    @Override
    protected void output(WnSystem sys, MailxContext fc) {
        // 如果设置，尝试转换变量
        NutMap mailVars = fc.vars;
        if (!Ws.isBlank(fc.varTrans)) {
            JsonFormat jfmt = JsonFormat.compact().setQuoteName(true);
            String varJson = Json.toJson(fc.vars, jfmt);
            varJson = sys.exec2(fc.varTrans, varJson);
            mailVars = Json.fromJson(NutMap.class, varJson);
        }
        fc.vars = mailVars;

        // 读取内容
        if (fc.mail.hasContentPath()) {
            WnObj oTmpl = fc.loadContentObj(fc.mail.getContentPath());
            String content = sys.io.readText(oTmpl);
            fc.mail.setContent(content);
        }

        // 应用变量
        fc.renderMail();

        // 发送
        try {
            WnMailPosting posting = new WnMailPosting(sys);
            posting.send(fc.config.smtp, fc.mail);
            AjaxReturn re = Ajax.ok().setData(fc.mail);
            tryPrintOutput(sys, fc, re);
        }
        catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.warn("Fail sendmail", e);
            }
            NutMap data = Wlang.map("email", fc.mail).setv("error", e.toString());
            if (null != e.getCause()) {
                data.put("cause", e.getCause().toString());
            }
            AjaxReturn re = Ajax.fail().setErrCode("e.cmd.mailx.FailToSend").setData(data);
            tryPrintOutput(sys, fc, re);

            throw e;
        }

    }

    private void tryPrintOutput(WnSystem sys, MailxContext fc, AjaxReturn re) {
        if (!fc.params.is("quiet")) {
            String output;
            // 作为 JSON 输出
            if (fc.params.is("json")) {
                if (re.isOk()) {
                    output = Json.toJson(re.getData(), fc.jfmt);
                } else {
                    output = Json.toJson(re, fc.jfmt);
                }
            }
            // 作为AJAX 输出
            else if (fc.params.is("ajax")) {
                output = Json.toJson(re, fc.jfmt);
            }
            // 作为纯文本输出
            else {
                output = fc.mail.toString();
            }

            // 打印成功
            if (re.isOk()) {
                sys.out.println(output);
            }
            // 打印错误
            else {
                sys.err.println(output);
            }
        }
    }

}
