package org.nutz.walnut.ext.net.sendmail.hdl;

import org.nutz.walnut.ext.net.sendmail.SendmailContext;
import org.nutz.walnut.ext.net.sendmail.bean.WnMailReceiver;

public class sendmail_fto extends sendmail_to_by_file {

    @Override
    protected void addReceivers(SendmailContext fc, WnMailReceiver[] res) {
        fc.mail.addMailToR(res);
    }

}
