package org.nutz.walnut.ext.net.mailx;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.net.mailx.bean.MailxConfig;
import org.nutz.walnut.ext.net.mailx.bean.WnSmtpMail;
import org.nutz.walnut.impl.box.JvmFilterContext;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.Wn;

public class MailxContext extends JvmFilterContext {

    public MailxConfig config;

    public WnSmtpMail mail;

    public String lang;

    public NutMap vars;

    public String varTrans;

    private WnObj _mail_home;

    /**
     * 子过滤器（譬如 `@imap`）如果开启了静默模式，那么 mailx 主命令就不会在结束前发送上下文中的邮件了<br>
     * 即发信和收信在本命令的生命周期中是互斥的。
     */
    private boolean quiet;

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

    public WnObj loadContentObj(String path) {
        WnObj oI18n = this.getI18nDir();
        if (null == oI18n || !oI18n.isDIR()) {
            return null;
        }
        String tLang = Ws.sBlank(this.lang, config.smtp.getLang());
        tLang = Ws.sBlank(tLang, "zh-cn");
        if (Ws.isBlank(tLang) || Ws.isBlank(path)) {
            return null;
        }
        String ph;
        if (Wn.isAbsolutePath(path)) {
            ph = path;
        } else {
            ph = Wn.appendPath(oI18n.path(), tLang, path);
        }
        String aph = Wn.normalizeFullPath(ph, sys);
        return sys.io.check(null, aph);
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

}
