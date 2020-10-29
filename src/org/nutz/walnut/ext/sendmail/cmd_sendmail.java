package org.nutz.walnut.ext.sendmail;

import org.nutz.walnut.ext.sendmail.bean.WnMail;
import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_sendmail extends JvmFilterExecutor<SendmailContext, SendmailFilter> {

    public cmd_sendmail() {
        super(SendmailContext.class, SendmailFilter.class);
    }

    @Override
    protected SendmailContext newContext() {
        return new SendmailContext();
    }

    @Override
    protected void prepare(WnSystem sys, SendmailContext fc) {
        fc.configName = fc.params.val(0, "_default");
        fc.mail = new WnMail();
    }

    @Override
    protected void output(WnSystem sys, SendmailContext fc) {}

}
