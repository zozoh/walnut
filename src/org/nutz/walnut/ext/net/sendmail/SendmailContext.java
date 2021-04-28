package org.nutz.walnut.ext.net.sendmail;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.net.sendmail.bean.WnMail;
import org.nutz.walnut.impl.box.JvmFilterContext;

public class SendmailContext extends JvmFilterContext {

    public String configName;

    public WnMail mail;

    public NutMap vars;

    public String varTrans;

    public NutMap toBeanForClient() {
        return Lang.map("mail", mail)
                   .setv("configName", configName)
                   .setv("vars", vars)
                   .setv("varTrans", varTrans);
    }

}
