package org.nutz.walnut.ext.sendmail;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.sendmail.bean.WnMail;
import org.nutz.walnut.impl.box.JvmFilterContext;

public class SendmailContext extends JvmFilterContext {

    public String configName;

    public WnMail mail;

    public NutMap vars;

}
