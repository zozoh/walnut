package org.nutz.walnut.util;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.validate.WnMatch;
import org.nutz.walnut.validate.impl.AlwaysMatch;
import org.nutz.walnut.validate.impl.AutoStrMatch;

public class Wobj {

    /**
     * 根据字段过滤器，生成一个过滤字段后的列表
     * 
     * @param list
     *            输入列表
     * @param km
     *            字段过滤器
     * @return 过滤字段后的对象列表
     */
    public static List<? extends NutBean> filterObjKeys(List<? extends NutBean> list, WnMatch km) {
        if (null == km) {
            return list;
        }

        List<NutBean> list2 = new ArrayList<>(list.size());
        for (NutBean o : list) {
            NutBean out = filterObjKeys(o, km);
            if (o.has("children")) {
                List<? extends NutBean> children = o.getAsList("children", NutBean.class);
                children = filterObjKeys(children, km);
                out.put("children", children);
            }
            list2.add(out);
        }
        return list2;
    }

    /**
     * 过滤对象字段
     * 
     * @param o
     *            输入对象
     * @param km
     *            字段过滤器
     * @return 过滤字段后的对象
     */
    public static NutBean filterObjKeys(NutBean o, WnMatch km) {
        if (null == o || null == km) {
            return o;
        }

        // zozoh 这个不判断不做了，用 gen_json_format 函数包含了这个逻辑
        // true 表示输出的时候，也显示双下划线开头的隐藏字段
        // boolean isShowAutoHide = params.is("H");

        // 依次判断字段
        NutMap map = new NutMap();
        for (String key : o.keySet()) {
            // 忽略自动隐藏字段
            // if (!isShowAutoHide && key.startsWith("__"))
            // continue;

            // 判断一下键
            if (km.match(key)) {
                map.put(key, o.get(key));
            }

        }
        return map;
    }

    public static String explainQuickObjKeyMatchStr(String str) {
        if (null == str)
            return null;

        String s = str.toUpperCase();
        // 快速字段: 扩展字段
        if (s.matches("[%#!]EXT")) {
            str = "!^(ph|race|ct|lm|sha1|data|d[0-9]"
                  + "|nm|pid|c|m|g|md|tp|mime"
                  + "|ln|mnt|expi|passwd|salt"
                  + "|th_(set|live|set_nm))$";
        }
        // 快速字段: 扩展字段加上 nm 字段
        else if (s.matches("[%#!]EXT-NM")) {
            str = "!^(ph|race|ct|lm|sha1|data|d[0-9]"
                  + "|pid|c|m|g|md|tp|mime"
                  + "|ln|mnt|expi|passwd|salt"
                  + "|th_(set|live|set_nm))$";
        }
        // 快速字段: 扩展字段加上 nm/tp 字段
        else if (s.matches("[%#!]EXT-TP")) {
            str = "!^(ph|race|ct|lm|sha1|data|d[0-9]"
                  + "|pid|c|m|g|md|mime"
                  + "|ln|mnt|expi|passwd|salt"
                  + "|th_(set|live|set_nm))$";
        }
        // 快速字段: 扩展字段加上 nm 字段以及时间字段和内容字段
        else if (s.matches("[%#!]EXT-TC")) {
            str = "!^(ph|race|data|d[0-9]"
                  + "|pid|c|m|g|md"
                  + "|ln|mnt|expi|passwd|salt"
                  + "|th_(set|live|set_nm))$";
        }
        // 快速字段: 扩展字段加上 nm 字段以及时间字段和内容字段以及 Thing 相关字段
        else if (s.matches("[%#!]EXT-THC")) {
            str = "!^(ph|race|data|d[0-9]|pid|c|m|g|md|ln|mnt|expi|passwd|salt)$";
        }
        // 原样返回
        return str;
    }

    public static WnMatch explainObjKeyMatcher(String str, WnMatch dft) {
        if (Strings.isBlank(str))
            return dft;

        // 分析 not
        boolean not = false;
        if (str.startsWith("!")) {
            not = true;
            str = str.substring(1).trim();
        }

        // 解析
        String str2 = explainQuickObjKeyMatchStr(str);

        // 得到实现类
        return new AutoStrMatch(str2, not);
    }

    public static WnMatch explainObjKeyMatcher(String str) {
        return explainObjKeyMatcher(str, new AlwaysMatch(true));
    }

}
