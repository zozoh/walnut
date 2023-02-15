package org.nutz.walnut.ext.net.mailx;

import org.nutz.walnut.ext.net.mailx.bean.MailxConfig;
import org.nutz.walnut.impl.box.JvmFilterContext;
import org.simplejavamail.api.email.EmailPopulatingBuilder;

public class MailxContext extends JvmFilterContext {
    
    public MailxConfig config;

    public EmailPopulatingBuilder builder;

}
