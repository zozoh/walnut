package org.nutz.walnut.ext.net.mailx;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.net.mailx.bean.MailxConfig;
import org.nutz.walnut.ext.net.mailx.bean.WnMail;
import org.nutz.walnut.impl.box.JvmFilterContext;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.Wn;

public class MailxContext extends JvmFilterContext {

    public MailxConfig config;

    public WnMail mail;

    public String lang;

    public NutMap vars;

    public String varTrans;

    private WnObj _mail_home;

    public void renderMail() {
        mail.render(vars);
    }

    WnObj getMailHome() {
        if (null == _mail_home) {
            _mail_home = Wn.checkObj(sys, "~/.mailx");
        }
        return _mail_home;
    }

    private WnObj _i18n_dir;

    WnObj getI18nDir() {
        if (null == _i18n_dir) {
            WnObj oHome = this.getMailHome();
            _i18n_dir = sys.io.fetch(oHome, "i18n");
        }
        return _i18n_dir;
    }

    public WnObj loadTemplateObj(String tmplName) {
        WnObj oI18n = this.getI18nDir();
        if (null == oI18n || !oI18n.isDIR()) {
            return null;
        }
        if (Ws.isBlank(lang) || Ws.isBlank(tmplName)) {
            return null;
        }

        return sys.io.fetch(oI18n, Wn.appendPath(lang, tmplName));
    }
}
