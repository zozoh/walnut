package org.nutz.walnut.ext.sendmail;

import org.apache.commons.mail.EmailException;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sendmail.api.WnMailApi;
import org.nutz.walnut.ext.sendmail.bean.WnMail;
import org.nutz.walnut.ext.sendmail.impl.WnMailService;
import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.ajax.Ajax;

public class cmd_sendmail extends JvmFilterExecutor<SendmailContext, SendmailFilter> {

    private static final Log log = Logs.get();

    public cmd_sendmail() {
        super(SendmailContext.class, SendmailFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(quiet|ajax)$");
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
            fc.mail.setLang(fc.params.get("lang"));
        }
        if (fc.params.has("base")) {
            fc.mail.setBaseUrl(fc.params.get("base"));
        }
    }

    @Override
    protected void output(WnSystem sys, SendmailContext fc) {
        // 读取主目录
        WnObj oHome = Wn.checkObj(sys, "~/.mail");

        // 准备服务类
        WnMailApi api = new WnMailService(sys.io, oHome, fc.configName);

        // 发送邮件
        boolean ok = false;
        Object re = fc.mail;
        try {
            api.smtp(fc.mail, fc.vars);
            ok = true;
            if (fc.params.is("ajax")) {
                re = Ajax.ok().setData(fc.mail);
            }
        }
        catch (EmailException e) {
            if (log.isDebugEnabled()) {
                log.warn("Fail sendmail", e);
            }
            re = Ajax.fail()
                     .setErrCode("e.cmd.sendmail.fail")
                     .setData(Lang.map("mail", fc.mail).setv("error", e));
        }

        // 输出
        if (!fc.params.is("quiet")) {
            String json = Json.toJson(re, fc.jfmt);
            if (ok) {
                sys.out.println(json);
            } else {
                sys.err.println(json);
            }
        }
    }

}
