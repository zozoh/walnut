package com.site0.walnut.impl.box.cmd;

import org.nutz.lang.Strings;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_echo extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, "en");

        boolean escape = params.is("e");
        boolean nonewline = params.is("n");

        // 有内容
        if (params.vals.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String s : params.vals) {
                if (escape)
                    s = Strings.evalEscape(s);
                // sb.append(Wn.normalizeStr(s, sys.se.vars())).append(' ');
                sb.append(s).append(' ');
            }
            if (sb.length() > 0)
                sb.deleteCharAt(sb.length() - 1);

            if (nonewline) {
                sys.out.print(sb.toString());
            } else {
                sys.out.println(sb.toString());
            }
        }
        // 没内容，写空
        else {
            if (!nonewline)
                sys.out.println("");
        }
    }

}
