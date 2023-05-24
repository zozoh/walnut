package org.nutz.walnut.ext.net.mailx.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.JsonFormat;
import org.nutz.walnut.ext.net.mailx.MailxContext;
import org.nutz.walnut.ext.net.mailx.MailxFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

import jakarta.mail.Flags;
import jakarta.mail.Flags.Flag;
import jakarta.mail.search.SearchTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.OrTerm;
import jakarta.mail.search.AndTerm;

public class mailx_imap extends MailxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(decrypt|or|json)$");
    }

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        JsonFormat jfmt = Cmds.gen_json_format(params);
        boolean isOr = params.is("or");
        boolean isDecrypt = params.is("decrypt");
        boolean isJson = params.is("json");

        // 分析查询 flag
        String[] ss = params.getAs("flags", String[].class);
        List<SearchTerm> terms = new ArrayList<>(ss.length);
        // Flags flags = new Flags();
        // 默认标签
        if (null == ss || ss.length == 0) {
            terms.add(new FlagTerm(new Flags(Flags.Flag.RECENT), true));
        }
        // 分析标签
        else {
            for (String f : params.vals) {
                if (f.startsWith("!")) {
                    f = f.substring(1);
                    terms.add(new FlagTerm(new Flags(f), false));
                } else {
                    terms.add(new FlagTerm(new Flags(f), true));
                }
            }
        }

        // 得到搜索条件
        SearchTerm search;

        if (terms.size() == 1) {
            search = terms.get(0);
        } else {
            SearchTerm[] tt = terms.toArray(new SearchTerm[terms.size()]);
            if (isOr) {
                search = new OrTerm(tt);
            } else {
                search = new AndTerm(tt);
            }
        }
    }

}
