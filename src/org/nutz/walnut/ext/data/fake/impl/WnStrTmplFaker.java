package org.nutz.walnut.ext.data.fake.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.walnut.ext.data.fake.WnFaker;
import org.nutz.walnut.ext.data.fake.WnFakes;

public class WnStrTmplFaker implements WnFaker<String> {

    private static Pattern _P = Pattern.compile("\\{([^}]+)\\}");

    private List<StfItem> items;

    public WnStrTmplFaker(String input, String lang) {
        items = new LinkedList<>();
        Matcher m = _P.matcher(input);
        int last = 0;
        while (m.find()) {
            int p0 = m.start();
            int p1 = m.end();

            // 记录之前
            if (p0 > last) {
                String s = input.substring(last, p0);
                items.add(new StaticStfItem(s));
            }

            // 标记当前项目
            String str = m.group(1).trim();
            WnFaker<?> faker = WnFakes.createFaker(str, lang);
            items.add(new FakerStfItem(faker));

            // 重新标记开始
            last = p1;
        }
        // 最后一段
        if (last < input.length()) {
            items.add(new StaticStfItem(input.substring(last)));
        }
    }

    @Override
    public String next() {
        StringBuilder sb = new StringBuilder();
        for (StfItem it : items) {
            sb.append(it.getString());
        }
        return sb.toString();
    }

    private static abstract class StfItem {
        abstract String getString();
    }

    private static class StaticStfItem extends StfItem {

        private String str;

        public StaticStfItem(String str) {
            this.str = str;
        }

        @Override
        String getString() {
            return str;
        }

    }

    private static class FakerStfItem extends StfItem {

        private WnFaker<?> faker;

        public FakerStfItem(WnFaker<?> faker) {
            this.faker = faker;
        }

        @Override
        String getString() {
            Object v = faker.next();
            if (v == null)
                return "";
            return v.toString();
        }

    }
}
