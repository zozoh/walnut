package org.nutz.walnut.util;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Strings;

public abstract class WStr {

    /**
     * @param strs
     *            输入参数，半角逗号分隔的字符串也会被分隔
     * @return 抚平后的字符串列表
     */
    public static List<String> flatList(String... strs) {
        List<String> list = new LinkedList<>();
        for (String s : strs) {
            String[] ss = Strings.splitIgnoreBlank(s);
            if (ss.length == 1) {
                list.add(ss[0]);
            } else {
                for (String s2 : ss) {
                    list.add(s2);
                }
            }
        }
        return list;
    }

    /**
     * @param strs
     *            输入参数，半角逗号分隔的字符串也会被分隔
     * @return 抚平后的字符串数组
     */
    public static String[] flatArray(String... strs) {
        List<String> list = flatList(strs);
        return list.toArray(new String[list.size()]);
    }

}
