package org.nutz.walnut.ext.data.fake.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.walnut.ext.data.fake.WnFaker;

public class WnIntTmplFaker implements WnFaker<String> {

    private List<TfItem> items;

    static Pattern _P = Pattern.compile("\\{(\\d+)-(\\d+)\\}");

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
                items.add(new StaticItem(s));
            }

            // 标记当前项目
            int min = Integer.parseInt(m.group(1));
            int max = Integer.parseInt(m.group(2));
            items.add(new FakerItem(min, max));

            // 重新标记开始
            last = p1;
        }
        // 最后一段
        if (last < input.length()) {
            items.add(new StaticItem(input.substring(last)));
        }
    }

    @Override
    public String next() {
        StringBuilder sb = new StringBuilder();
        for (TfItem it : items) {
            sb.append(it.getString());
        }
        return sb.toString();
    }

    static abstract class TfItem {
        abstract String getString();
    }

    static class StaticItem extends TfItem {

        private String str;

        public StaticItem(String str) {
            this.str = str;
        }

        @Override
        String getString() {
            return str;
        }

    }

    static class FakerItem extends TfItem {

        private WnFaker<Integer> faker;

        public FakerItem(int min, int max) {
            faker = new WnIntegerFaker(min, max);
        }

        @Override
        String getString() {
            return faker.next() + "";
        }

    }
}
