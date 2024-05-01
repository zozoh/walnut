package com.site0.walnut.ext.data.fake.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.site0.walnut.ext.data.fake.WnFaker;
import com.site0.walnut.util.Ws;

public class WnHexTmplFaker implements WnFaker<String> {
    private static Pattern _P = Pattern.compile("^([0-9]+[LR].?):(.+)$", Pattern.CASE_INSENSITIVE);
    private static Pattern _P_IT = Pattern.compile("\\{([0-9A-F]+)-([0-9A-F]+)\\}",
                                                   Pattern.CASE_INSENSITIVE);

    private List<HexTmplItem> items;

    private WnPadFaker padFaker;

    private boolean upperCase;

    public WnHexTmplFaker(String input, boolean upper) {
        this.upperCase = upper;
        // 看看是不是这种模式: 2R0:#{0-FF}{0-FF}{0-FF}
        Matcher m = _P.matcher(input);
        if (m.find()) {
            String pads = m.group(1);
            input = m.group(2);
            this.setPadFaker(pads);
        }

        // 循环解析
        items = new LinkedList<>();
        m = _P_IT.matcher(input);
        int last = 0;
        while (m.find()) {
            int p0 = m.start();
            int p1 = m.end();

            // 记录之前
            if (p0 > last) {
                String s = input.substring(last, p0);
                items.add(new StaticHexTmplItem(s));
            }

            // 标记当前项目
            int min = Integer.parseInt(m.group(1), 16);
            int max = Integer.parseInt(m.group(2), 16);
            DynamicHexTmplItem df = new DynamicHexTmplItem(min, max, padFaker, this.isUpperCase());
            items.add(df);

            // 重新标记开始
            last = p1;
        }
        // 最后一段
        if (last < input.length()) {
            items.add(new StaticHexTmplItem(input.substring(last)));
        }
    }

    public void setPadFaker(String pads) {
        if (!Ws.isQuoteBy(pads, '[', ']')) {
            pads = String.format("[%s]", pads);
        }
        this.padFaker = new WnPadFaker(null, pads);
    }

    @Override
    public String next() {
        StringBuilder sb = new StringBuilder();
        for (HexTmplItem it : items) {
            sb.append(it.getString());
        }
        return sb.toString();
    }

    public boolean isUpperCase() {
        return upperCase;
    }

    public void setUpperCase(boolean upperCase) {
        this.upperCase = upperCase;
    }

    private static abstract class HexTmplItem {
        abstract String getString();
    }

    private static class StaticHexTmplItem extends HexTmplItem {

        private String str;

        public StaticHexTmplItem(String str) {
            this.str = str;
        }

        @Override
        String getString() {
            return str;
        }

    }

    private static class DynamicHexTmplItem extends HexTmplItem {

        private WnFaker<String> faker;

        private boolean upperCase;

        public DynamicHexTmplItem(int min, int max, WnPadFaker padFaker, boolean upper) {
            this.upperCase = upper;
            // 需要自动填充
            if (null != padFaker) {
                WnPadFaker pf = padFaker.clone();
                pf.setFaker(new WnHexFaker(min, max));
                faker = pf;
            }
            // 默认
            else {
                faker = new WnHexFaker(min, max);
            }
        }

        @Override
        String getString() {
            String str = faker.next();
            if (upperCase) {
                return str.toUpperCase();
            }
            return str;
        }

    }
}
