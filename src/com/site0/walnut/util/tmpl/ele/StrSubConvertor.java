package com.site0.walnut.util.tmpl.ele;

import com.site0.walnut.util.Ws;

public class StrSubConvertor implements StrEleConvertor {

    private int from;
    private int len;

    StrSubConvertor(String input) {
        String[] poss = Ws.splitIgnoreBlank(input, "/");

        // @sub=5 表示 substring(0,5)
        if (poss.length == 1) {
            from = 0;
            len = Integer.parseInt(poss[0]);
        } else if (poss.length > 1) {
            from = Integer.parseInt(poss[0]);
            len = Integer.parseInt(poss[1]);
        }
    }

    @Override
    public String process(String str) {
        return str.substring(from, len);
    }

}
