package org.nutz.walnut.ext.sendmail.hdl;

import org.nutz.walnut.ext.sendmail.SendmailContext;
import org.nutz.walnut.ext.sendmail.bean.WnMailReceiver;

public class sendmail_fcc extends sendmail_to_by_file {

    @Override
    protected void addReceivers(SendmailContext fc, WnMailReceiver[] res) {
        fc.mail.addCC(res);
    }

}