package com.site0.walnut.ext.data.fake.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.site0.walnut.ext.data.fake.WnFaker;

public class WnIntTmplFaker implements WnFaker<String> {

    private static Pattern _P = Pattern.compile("\\{(\\d+)-(\\d+)\\}");

    private List<ItfItem> items;

    public WnIntTmplFaker(String input) {
        items = new LinkedList<>();
        Matcher m = _P.matcher(input);
        int last = 0;
        while (m.find()) {
            int p0 = m.start();
            int p1 = m.end();

            // 记录之前
            if (p0 > last) {
                String s = input.substring(last, p0);
                items.add(new StaticItfItem(s));
            }

            // 标记当前项目
            int min = Integer.parseInt(m.group(1));
            int max = Integer.parseInt(m.group(2));
            items.add(new FakerItfItem(min, max));

            // 重新标记开始
            last = p1;
        }
        // 最后一段
        if (last < input.length()) {
            items.add(new StaticItfItem(input.substring(last)));
        }
    }

    @Override
    public String next() {
        StringBuilder sb = new StringBuilder();
        for (ItfItem it : items) {
            sb.append(it.getString());
        }
        return sb.toString();
    }

    private static abstract class ItfItem {
        abstract String getString();
    }

    private static class StaticItfItem extends ItfItem {

        private String str;

        public StaticItfItem(String str) {
            this.str = str;
        }

        @Override
        String getString() {
            return str;
        }

    }

    private static class FakerItfItem extends ItfItem {

        private WnFaker<Integer> faker;

        public FakerItfItem(int min, int max) {
            faker = new WnIntegerFaker(min, max);
        }

        @Override
        String getString() {
            return faker.next() + "";
        }

    }
}
