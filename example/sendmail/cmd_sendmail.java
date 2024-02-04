package org.nutz.walnut.ext.net.sendmail;

import org.apache.commons.mail.EmailException;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.walnut.util.Wlog;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.net.sendmail.api.WnMailApi;
import org.nutz.walnut.ext.net.sendmail.bean.WnMail;
import org.nutz.walnut.ext.net.sendmail.impl.WnMailService;
import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

public class cmd_sendmail extends JvmFilterExecutor<SendmailContext, SendmailFilter> {

    private static final Log log = Wlog.getCMD();

    public cmd_sendmail() {
        super(SendmailContext.class, SendmailFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(quiet|ajax|json)$");
    }

    @Override
    protected SendmailContext newContext() {
        return new SendmailContext();
    }

    @Override
    protected void prepare(WnSystem sys, SendmailContext fc) {
        fc.configName = fc.params.val(0, "_default");
        fc.mail = new WnMail();
        fc.vars = new NutMap();
        if (fc.params.has("lang")) {
            fc.mail.setLang(fc.params.getString("lang"));
        }
        if (fc.params.has("base")) {
            fc.mail.setBaseUrl(fc.params.get("base"));
        }
    }

    @Override
    protected void output(WnSystem sys, SendmailContext fc) {
        // 如果设置，尝试转换变量
        NutMap mailVars = fc.vars;
        if (!Strings.isBlank(fc.varTrans)) {
            String varJson = Json.toJson(fc.vars, JsonFormat.compact().setQuoteName(true));
            varJson = sys.exec2(fc.varTrans, varJson);
            mailVars = Json.fromJson(NutMap.class, varJson);
        }

        // 读取主目录
        WnObj oHome = Wn.checkObj(sys, "~/.mail");

        // 准备服务类
        WnMailApi api = new WnMailService(sys.io, oHome, fc.configName);

        // 发送邮件
        boolean ok = false;
        Object re = fc.toBeanForClient();
        try {
            api.smtp(fc.mail, mailVars);
            ok = true;
            if (fc.params.is("ajax")) {
                re = Ajax.ok().setData(re);
            }
        }
        catch (EmailException e) {
            if (log.isDebugEnabled()) {
                log.warn("Fail sendmail", e);
            }
            NutMap data = Lang.map("context", re).setv("error", e.toString());
            if (null != e.getCause()) {
                data.put("cause", e.getCause().toString());
            }
            re = Ajax.fail().setErrCode("e.cmd.sendmail.fail").setData(data);

        }

        // 输出
        if (!fc.params.is("quiet")) {
            // 作为 JSON 输出
            if (fc.params.is("json") || (re instanceof AjaxReturn)) {
                String json = Json.toJson(re, fc.jfmt);
                if (ok) {
                    sys.out.println(json);
                } else {
                    sys.err.println(json);
                }
            }
            // 作为纯文本输出
            else {
                String str = fc.mail.toString(mailVars);
                sys.out.println(str);
            }
        }
    }

}
