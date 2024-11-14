package com.site0.walnut.util.tmpl.ele;

public class StrSubConvertor implements StrEleConvertor {

    private int from;
    private int to;

    StrSubConvertor(String input) {
        String[] poss = input.trim().split("/");

        // @sub=2/ => [2,NaN] 表示 substring(2)
        // @sub=5 => [5] 表示 substring(0,5)
        if (poss.length == 1) {
            // @sub=2/ => [2,NaN] 表示 substring(2)
            if (input.endsWith("/")) {
                from = Integer.parseInt(poss[0]);
                to = -1;
            }
            // @sub=5 => [5] 表示 substring(0,5)
            else {
                from = 0;
                to = Integer.parseInt(poss[0]);
            }
        }
        // @sub=2/8 表示 substring(2,8)
        else if (poss.length > 1) {
            try {
                from = Integer.parseInt(poss[0]);
            }
            catch (NumberFormatException e) {
                from = -1;
            }
            try {
                to = Integer.parseInt(poss[1]);
            }
            catch (NumberFormatException e) {
                to = -1;
            }
            // @sub=/5 => [NaN, 5] 表示 substring(0,5)
            // @sub=2/8` => [2,8] 表示 substring(2,8)
            if (from < 0) {
                from = 0;
            }
        }
        // 维持原样
        else {
            from = -1;
        }
    }

    @Override
    public String process(String str) {
        int p0 = Math.max(0, Math.min(from, str.length() - 1));
        int p1 = to < 0 ? str.length() + 1 + to : Math.min(str.length(), to);

        if (null == str || p0 >= p1) {
            return "";
        }

        return str.substring(p0, p1);
    }

}
