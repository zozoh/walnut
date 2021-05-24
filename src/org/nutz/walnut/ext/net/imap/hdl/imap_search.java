package org.nutz.walnut.ext.net.imap.hdl;

import java.util.List;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.search.FlagTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.net.imap.ImapContext;
import org.nutz.walnut.ext.net.imap.ImapFilter;
import org.nutz.walnut.ext.net.imap.cmd_imap;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.ZParams;

public class imap_search extends ImapFilter {

    @Override
    protected void process(WnSystem sys, ImapContext fc, ZParams params) {
        //
        // 默认是未读邮件
        //
        String[] flagList = Wlang.array("0:seen");
        if (params.vals.length > 0) {
            flagList = params.vals;
        }

        //
        // 准备搜索条件
        //
        List<FlagTerm> termList = cmd_imap.parseFlagTermList(flagList);
        SearchTerm st = toSearchTerm(termList);

        // 搜索
        try {
            fc.setMessages(fc.folder.search(st));
        }
        catch (MessagingException e) {
            throw Er.create("e.cmd.imap.search.messages", e);
        }
    }

    private SearchTerm toSearchTerm(List<FlagTerm> termList) {
        SearchTerm st;
        // 没有指定任何有效标记
        if (termList.isEmpty()) {
            st = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        }
        // 只有一个标记
        else if (termList.size() == 1) {
            st = termList.get(0);
        }
        // 多个标记
        else {
            SearchTerm[] terms = new SearchTerm[termList.size()];
            termList.toArray(terms);
            st = new OrTerm(terms);
        }
        return st;
    }

}
