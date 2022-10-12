package org.nutz.walnut.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AlwaysMatch;
import org.nutz.walnut.util.validate.impl.AutoStrMatch;

public class Wobj {

    public static String[] CORE_FIELDS = Wlang.array("id",
                                                     "pid",
                                                     "race",
                                                     "ct",
                                                     "lm",
                                                     "d0",
                                                     "d1",
                                                     "pvg",
                                                     "md",
                                                     "c",
                                                     "g",
                                                     "m",
                                                     "ph",
                                                     "thumb",
                                                     "synt",
                                                     "expi",
                                                     "nano",
                                                     "mnt",
                                                     "len",
                                                     "sha1");

    /**
     * 根据字段过滤器，生成一个过滤字段后的列表
     * 
     * @param list
     *            对象列表
     * @param kms
     *            字段过滤器（必须全部符合）
     * @param autoPath
     *            自动检查对象，是否都加载好了路径
     * @return 过滤字段后的对象列表
     */
    public static List<? extends NutBean> filterObjKeys(List<WnObj> list,
                                                        WnMatch[] kms,
                                                        boolean autoPath) {
        return filterObjKeys(list, kms, "children", autoPath);
    }

    /**
     * 根据字段过滤器，生成一个过滤字段后的列表
     * 
     * @param list
     *            对象列表
     * @param kms
     *            字段过滤器（必须全部符合）
     * @param subKey
     *            哪个字段表示对象的 ID
     * @param autoPath
     *            自动检查对象，是否都加载好了路径
     * @return 过滤字段后的对象列表
     */
    public static List<? extends NutBean> filterObjKeys(List<WnObj> list,
                                                        WnMatch[] kms,
                                                        String subKey,
                                                        boolean autoPath) {
        // 自动加载路径字段
        if (autoPath && null != list && !list.isEmpty()) {
            boolean canMatchPath = null == kms || kms.length == 0;
            if (!canMatchPath) {
                canMatchPath = true;
                for (WnMatch km : kms) {
                    if (!km.match("ph")) {
                        canMatchPath = true;
                        break;
                    }
                }
            }
            if (canMatchPath) {
                for (WnObj o : list) {
                    o.path();
                }
            }
        }
        return filterObjKeys(list, kms, "id", subKey);
    }

    /**
     * 根据字段过滤器，生成一个过滤字段后的列表
     * 
     * @param list
     *            输入列表
     * @param kms
     *            字段过滤器（必须全部符合）
     * @param subKey
     *            哪个字段表示对象的 ID，如果是 null，则表示 "children"
     * @param subKey
     *            哪个字段表示子节点列表
     * @return 过滤字段后的对象列表
     */
    public static List<? extends NutBean> filterObjKeys(List<? extends NutBean> list,
                                                        WnMatch[] kms,
                                                        String idKey,
                                                        String subKey) {
        // 无需过滤
        if (null == kms || kms.length == 0) {
            return list;
        }

        // 默认的 idKey
        idKey = Ws.sBlank(idKey, "id");

        // 默认的 subKey
        subKey = Ws.sBlank(subKey, "children");

        // 这个集合用来防止无限递归
        Map<String, NutBean> memo = new HashMap<>();

        // 开始吧 ...
        return __flt_obj_key(list, kms, idKey, subKey, memo);
    }

    public static List<? extends NutBean> __flt_obj_key(List<? extends NutBean> list,
                                                        WnMatch[] kms,
                                                        String idKey,
                                                        String subKey,
                                                        Map<String, NutBean> memo) {
        List<NutBean> list2 = new ArrayList<>(list.size());
        for (NutBean o : list) {
            String id = o.getString(idKey);
            // 阻止无限递归
            if (memo.containsKey(id)) {
                continue;
            }

            // 过滤字段
            NutBean out = filterObjKeys(o, kms);

            // 递归子节点
            if (o.has(subKey)) {
                // 阻止子节点无穷递归
                memo.put(id, o);
                // 处理子节点
                List<? extends NutBean> children = o.getAsList(subKey, NutBean.class);
                children = __flt_obj_key(children, kms, idKey, subKey, memo);
                out.put(subKey, children);
                // 嗯，搞定
                memo.remove(id);
            }

            // 计入结果
            list2.add(out);
        }
        return list2;
    }

    /**
     * 过滤对象字段
     * 
     * @param o
     *            输入对象
     * @param kms
     *            字段过滤器（必须全部符合）
     * @return 过滤字段后的对象
     */
    public static NutBean filterObjKeys(NutBean o, WnMatch... kms) {
        if (null == o || null == kms || kms.length == 0) {
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
            boolean ignore = false;
            for (WnMatch km : kms) {
                if (!km.match(key)) {
                    ignore = true;
                    break;
                }
            }
            if (!ignore) {
                map.put(key, o.get(key));
            }

        }
        return map;
    }

    private static final Pattern P_QUICK_KEYS = Regex.getPattern("^[%#]([A-Z0-9]+)?$");

    public static String explainQuickObjKeyMatchStr(String str) {
        if (null == str)
            return null;

        String s = str.toUpperCase();
        Matcher m = P_QUICK_KEYS.matcher(s);
        if (m.find()) {
            String md = m.group(1);

            // 快速字段: 扩展字段加上 nm 字段
            if ("NM".equals(md)) {
                str = "!^(ph|race|ct|lm|sha1|data|d[0-9]|local"
                      + "|pid|c|m|g|md|tp|mime"
                      + "|ln|mnt|expi|passwd|salt"
                      + "|th_(set|live|set_nm))$";
            }
            // 快速字段: 扩展字段加上 nm/tp 字段
            else if ("TP".equals(md)) {
                str = "!^(ph|race|ct|lm|sha1|data|d[0-9]|local"
                      + "|pid|c|m|g|md|mime"
                      + "|ln|mnt|expi|passwd|salt"
                      + "|th_(set|live|set_nm|auto_select))$";
            }
            // 快速字段: 扩展字段加上 nm/tp/ph 字段
            else if ("PH".equals(md)) {
                str = "!^(race|ct|lm|sha1|data|d[0-9]|local"
                      + "|pid|c|m|g|md|mime"
                      + "|ln|mnt|expi|passwd|salt"
                      + "|th_(set|live|set_nm|auto_select))$";
            }
            // 快速字段: 扩展字段加上 nm/tp/ct 字段
            else if ("TPCT".equals(md)) {
                str = "!^(ph|race|lm|sha1|data|d[0-9]|local"
                      + "|pid|c|m|g|md|mime"
                      + "|ln|mnt|expi|passwd|salt"
                      + "|th_(set|live|set_nm|auto_select))$";
            }
            // 快速字段: 扩展字段加上 nm/tp/pid 字段
            else if ("PID".equals(md)) {
                str = "!^(ph|race|ct|lm|sha1|data|d[0-9]|local"
                      + "|c|m|g|md|mime"
                      + "|ln|mnt|expi|passwd|salt"
                      + "|th_(set|live|set_nm|auto_select))$";
            }
            // 快速字段: 扩展字段加上 nm/tp/pid 字段
            else if ("Q".equals(md)) {
                str = "!^(ph|race|ct|lm|sha1|data|d[0-9]|local"
                      + "|c|m|g|md|mime"
                      + "|ln|mnt|expi|passwd|salt"
                      + "|th_(set|live|set_nm|auto_select))$";
            }
            // 快速字段: 扩展字段加上 nm/tp/sha1/mime 等字段
            else if ("SHA1".equals(md)) {
                str = "!^(ph|race|ct|data|d[0-9]|local"
                      + "|pid|c|m|g|md"
                      + "|ln|mnt|expi|passwd|salt"
                      + "|th_(set|live|set_nm|auto_select))$";
            }
            // 快速字段: 扩展字段加上 nm 字段以及时间字段和内容字段
            else if ("TC".equals(md)) {
                str = "!^(ph|race|data|d[0-9]|local"
                      + "|pid|c|m|g|md"
                      + "|ln|mnt|expi|passwd|salt"
                      + "|th_(set|live|set_nm|auto_select))$";
            }
            // 快速字段: 扩展字段加上 nm 字段以及时间字段和内容字段以及 Thing 相关字段
            else if ("THC".equals(md)) {
                str = "!^(ph|race|data|d[0-9]|local|pid|c|m|g|md|ln|mnt|expi|passwd|salt)$";
            }
            // 默认快速字段: 扩展字段
            else {
                str = "!^(ph|race|ct|lm|sha1|data|d[0-9]|local"
                      + "|nm|pid|c|m|g|md|tp|mime"
                      + "|ln|mnt|expi|passwd|salt"
                      + "|th_(set|live|set_nm|auto_select))$";
            }
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

    public static WnMatch explainObjKeyMatcher(String str, boolean dft) {
        return explainObjKeyMatcher(str, new AlwaysMatch(dft));
    }

    public static String evalName(String name, String id) {
        name = Strings.sBlank(name, "${id}");
        return Tmpl.exec(name, Lang.map("id", id));
    }

    public static boolean isValidName(String name) {
        // 名称不能包括特殊符号
        if (name.matches("^.*([/\\\\*?#&^%;`'\"]+).*$")) {
            return false;
        }
        return true;
    }

    public static void assertValidName(String name) {
        // 名称不能为空
        if (Strings.isBlank(name)) {
            throw Er.create("e.io.obj.BlankName");
        }
        // 名称不能包括特殊符号
        if (!isValidName(name)) {
            throw Er.create("e.io.obj.InvalidName", name);
        }
    }

    public static String normalizeName(String name) {
        // 名称不能为空
        if (Strings.isBlank(name)) {
            return "BLANK_OBJ_NAME";
        }
        // 名称不能包括特殊符号
        if (!isValidName(name)) {
            return name.replaceAll("([/\\\\*?#&^%;`'\"]+)", "_");
        }
        return name;
    }

}
