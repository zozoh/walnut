package org.nutz.walnut.ext.net.sendmail.hdl;

import org.nutz.lang.Strings;
import org.nutz.walnut.ext.net.sendmail.SendmailContext;
import org.nutz.walnut.ext.net.sendmail.SendmailFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class sendmail_trans extends SendmailFilter {

    @Override
    protected void process(WnSystem sys, SendmailContext fc, ZParams params) {
        fc.varTrans = Strings.join(" ", params.args);
    }

}
