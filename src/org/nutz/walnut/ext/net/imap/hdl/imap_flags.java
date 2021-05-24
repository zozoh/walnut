package org.nutz.walnut.ext.net.imap.hdl;

import java.util.List;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.search.FlagTerm;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.net.imap.ImapContext;
import org.nutz.walnut.ext.net.imap.ImapFilter;
import org.nutz.walnut.ext.net.imap.cmd_imap;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class imap_flags extends ImapFilter {

    @Override
    protected void process(WnSystem sys, ImapContext fc, ZParams params) {
        // 防守
        if (!fc.hasMessages()) {
            return;
        }

        // 解析标记
        List<FlagTerm> termList = cmd_imap.parseFlagTermList(params.vals);

        // 设置
        try {
            for (FlagTerm ft : termList) {
                Flags flags = ft.getFlags();
                boolean yes = ft.getTestSet();

                fc.folder.setFlags(fc.messages, flags, yes);
            }
        }
        catch (MessagingException e) {
            throw Er.create("e.cmd.imap.flags", e);
        }
    }

}
