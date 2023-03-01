package org.nutz.walnut.ext.net.imap.hdl;

import javax.mail.MessagingException;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.net.imap.ImapContext;
import org.nutz.walnut.ext.net.imap.ImapFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class imap_summary extends ImapFilter {

    @Override
    protected void process(WnSystem sys, ImapContext fc, ZParams params) {
        try {
            fc.summary = new NutMap();
            fc.summary.put("fullName", fc.folder.getFullName());
            fc.summary.put("name", fc.folder.getName());
            fc.summary.put("unread", fc.folder.getUnreadMessageCount());
            fc.summary.put("deleted", fc.folder.getDeletedMessageCount());
            fc.summary.put("recent", fc.folder.getNewMessageCount());
            fc.summary.put("total", fc.folder.getMessageCount());
        }
        catch (MessagingException e) {
            throw Er.create("e.cmd.imap.summary", e);
        }
    }

}
