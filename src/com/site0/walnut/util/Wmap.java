package com.site0.walnut.util;

import java.util.HashMap;
import java.util.Map;

public abstract class Wmap {

    /**
     * 解析映射关系
     * 
     * @param input
     *            简易字符串描述的映射关系 <code>a:A1;b:B1</code> 即，将输入的 "a" 键变成 "A1"， "b"
     *            键变成 "B1"
     * @return 一个映射关系
     */
    public static Map<String, String> parseMapping(String input) {
        Map<String, String> re = new HashMap<>();
        String[] ss = Ws.splitIgnoreBlank(input, "[;,]");
        for (String s : ss) {
            String[] kk = Ws.splitIgnoreBlank(s, ":");
            if (kk.length == 2) {
                re.put(kk[0], kk[1]);
            }
        }
        return re;
    }

    public static <T extends Map<String, Object>> void doMapping(T input,
                                                                 T output,
                                                                 Map<String, String> mapping) {
        if (null == input || null == mapping || mapping.isEmpty())
            return;
        for (Map.Entry<String, Object> en : input.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            String k2 = mapping.getOrDefault(key, key);
            output.put(k2, val);
        }
    }

}
