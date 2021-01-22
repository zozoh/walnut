package org.nutz.walnut.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

/**
 * 字符串帮助类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class Ws {

    /**
     * 测试此字符串是否被指定的左字符和右字符所包裹；如果该字符串左右两边有空白的时候，会首先忽略这些空白
     *
     * @param cs
     *            字符串
     * @param lc
     *            左字符
     * @param rc
     *            右字符
     * @return 字符串是被左字符和右字符包裹
     */
    public static boolean isQuoteByIgnoreBlank(CharSequence cs, char lc, char rc) {
        if (null == cs)
            return false;
        int len = cs.length();
        if (len < 2)
            return false;
        int l = 0;
        int last = len - 1;
        int r = last;
        for (; l < len; l++) {
            if (!Character.isWhitespace(cs.charAt(l)))
                break;
        }
        if (cs.charAt(l) != lc)
            return false;
        for (; r > l; r--) {
            if (!Character.isWhitespace(cs.charAt(r)))
                break;
        }
        return l < r && cs.charAt(r) == rc;
    }

    /**
     * 测试此字符串是否被指定的左字符和右字符所包裹
     *
     * @param cs
     *            字符串
     * @param lc
     *            左字符
     * @param rc
     *            右字符
     * @return 字符串是被左字符和右字符包裹
     */
    public static boolean isQuoteBy(CharSequence cs, char lc, char rc) {
        if (null == cs)
            return false;
        int length = cs.length();
        return length > 1 && cs.charAt(0) == lc && cs.charAt(length - 1) == rc;
    }

    /**
     * 测试此字符串是否被指定的左字符串和右字符串所包裹
     *
     * @param str
     *            字符串
     * @param l
     *            左字符串
     * @param r
     *            右字符串
     * @return 字符串是被左字符串和右字符串包裹
     */
    public static boolean isQuoteBy(String str, String l, String r) {
        if (null == str || null == l || null == r)
            return false;
        return str.startsWith(l) && str.endsWith(r);
    }

    /**
     * 统计某字符在给定字符串中出现的次数
     * 
     * @param cs
     *            字符串
     * @param c
     *            字符
     * @return 字符出现次数
     */
    public static int countChar(CharSequence cs, char c) {
        int re = 0;
        int len = cs.length();
        for (int i = 0; i < len; i++) {
            if (cs.charAt(i) == c) {
                re++;
            }
        }
        return re;
    }

    /**
     * 将容器内对象合并为一个字符串
     * 
     * @param arr
     *            字符串数组
     * @param sep
     *            分隔符
     * @return 合并后的字符串
     */
    public static String join(String[] arr, String sep) {
        StringBuilder sb = new StringBuilder();
        if (arr.length > 0) {
            sb.append(arr[0]);

            for (int i = 1; i < arr.length; i++) {
                sb.append(sep);
                sb.append(arr[i]);
            }
        }
        return sb.toString();
    }

    /**
     * 将字符串按半角逗号，拆分成数组，空元素将被忽略
     *
     * @param s
     *            字符串
     * @return 字符串数组
     */
    public static String[] splitIgnoreBlank(String s) {
        return splitIgnoreBlank(s, ",");
    }

    /**
     * 将字符串按半角逗号，拆分成数组，空元素将被忽略
     *
     * @param s
     *            字符串
     * @return 字符串列表
     */
    public static List<String> splitIgnoreBlanks(String s) {
        return splitIgnoreBlanks(s, ",");
    }

    /**
     * 根据一个正则式，将字符串拆分成数组，空元素将被忽略
     *
     * @param s
     *            字符串
     * @param regex
     *            正则式
     * @return 字符串数组
     */
    public static String[] splitIgnoreBlank(String s, String regex) {
        if (null == s)
            return null;
        String[] ss = s.split(regex);
        List<String> list = new LinkedList<String>();
        for (String st : ss) {
            if (isBlank(st))
                continue;
            list.add(trim(st));
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 根据一个正则式，将字符串拆分成数组，空元素将被忽略
     *
     * @param s
     *            字符串
     * @param regex
     *            正则式
     * @return 字符串列表
     */
    public static List<String> splitIgnoreBlanks(String s, String regex) {
        if (null == s)
            return null;
        String[] ss = s.split(regex);
        List<String> list = new LinkedList<String>();
        for (String st : ss) {
            if (isBlank(st))
                continue;
            list.add(trim(st));
        }
        return list;
    }

    private static char[] STR_ESC_TS = Wchar.array('n',
                                                   '\n',
                                                   'r',
                                                   '\r',
                                                   't',
                                                   '\t',
                                                   'b',
                                                   '\b',
                                                   '\'',
                                                   '\'',
                                                   '"',
                                                   '"',
                                                   '\\',
                                                   '\\');

    // 解码过程表: 转义字符串 -> 真正字符
    private static final Wchar.EscapeTable STR_UNESC_TAB = Wchar.buildEscapeTable(STR_ESC_TS);

    // 编码过程表: 真正字符 -> 转义字符串
    private static final Wchar.EscapeTable STR_ESC_TAB = Wchar.buildEscapeReverTable(STR_ESC_TS);

    /**
     * 将字符串根据转移字符转义
     *
     * @param str
     *            输入字符串
     * @return 转义后的字符串
     */
    public static String escape(String str) {
        return escape(str, '\\', STR_ESC_TAB);
    }

    /**
     * 将字符串根据转移字符转义
     *
     * @param str
     *            输入字符串
     * @param escape
     *            触发转义的字符
     * @param table
     *            字符转义表
     * @return 转义后的字符串
     */
    public static String escape(String str, char escape, Wchar.EscapeTable table) {
        StringBuilder sb = new StringBuilder();
        char[] cs = str.toCharArray();
        for (int i = 0; i < cs.length; i++) {
            char c = cs[i];
            char e_c = table.get(c);
            // 如果是转义字符
            if (e_c != 0) {
                sb.append(escape).append(e_c);
            }
            // 否则添加
            else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 将字符串根据转移字符转义
     *
     * @param str
     *            输入字符串
     * @return 转义后的字符串
     */
    public static String unescape(String str) {
        return unescape(str, '\\', STR_UNESC_TAB);
    }

    /**
     * 将字符串根据转移字符转义
     *
     * @param str
     *            输入字符串
     * @param escape
     *            触发转义的字符
     * @param table
     *            字符转义表
     * @return 转义后的字符串
     */
    public static String unescape(String str, char escape, Wchar.EscapeTable table) {
        StringBuilder sb = new StringBuilder();
        char[] cs = str.toCharArray();
        for (int i = 0; i < cs.length; i++) {
            char c = cs[i];
            // 如果是转义字符
            if (c == escape) {
                c = cs[++i];
                char c2 = table.get(c);
                if (0 == c2) {
                    throw Lang.makeThrow("evalEscape invalid char[%d] '%c'  : %s", i, c, str);
                }
                sb.append(c2);
            }
            // 否则添加
            else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 将字符串按照某个或几个分隔符拆分。 其中，遇到字符串 "xxx" 或者 'xxx' 并不拆分。
     *
     * @param str
     *            要被拆分的字符串
     * @param quote
     *            支持的引号字符集合
     * @param seps
     *            分隔符集合
     * @return 拆分后的列表
     * 
     * @see #splitQuote(String, String, boolean, boolean, String)
     */
    public static List<String> splitQuote(String str, String quote, String seps) {
        return splitQuote(str, quote, '\\', false, true, seps);
    }

    /**
     * 将字符串按照某个或几个分隔符拆分。 其中，遇到字符串 "xxx" 或者 'xxx' 并不拆分
     *
     * @param str
     *            要被拆分的字符串
     * @param quote
     *            支持的引号字符集合
     * @param eacapeChar
     *            转义字符，在 quote,可以逃逸结束引用
     * @param ignoreBlank
     *            是否忽略空白项目
     * @param keepQuote
     *            是否保持引号
     * @param seps
     *            分隔符集合
     * @return 拆分后的列表
     */
    public static List<String> splitQuote(String str,
                                          String quote,
                                          char eacapeChar,
                                          boolean ignoreBlank,
                                          boolean keepQuote,
                                          String ss) {
        char[] qucs = quote.toCharArray();
        char[] seps = ss.toCharArray();
        List<String> list = new LinkedList<String>();
        char[] cs = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        char quoteBy = 0;
        for (int i = 0; i < cs.length; i++) {
            char c = cs[i];
            // 引用外，且遇到分隔符号就拆分
            if (0 == quoteBy && Wchar.isIn(seps, c)) {
                if (!ignoreBlank || !Ws.isBlank(sb)) {
                    String s2 = sb.toString();
                    if (ignoreBlank)
                        s2 = Ws.trim(s2);
                    list.add(s2);
                    sb = new StringBuilder();
                }
                continue;
            }
            // 在引用里
            if (0 != quoteBy) {
                // 结束引用
                if (quoteBy == c) {
                    if (keepQuote) {
                        sb.append(c);
                    }
                    list.add(sb.toString());
                    quoteBy = 0;
                    sb = new StringBuilder();
                    continue;
                }
                // 逃逸字符
                if (eacapeChar == c) {
                    sb.append(c);
                    if (i < cs.length) {
                        c = cs[++i];
                        sb.append(c);
                    }
                    continue;
                }
            }
            // 开始引用
            if (Wchar.isIn(qucs, c)) {
                // 开始引用
                quoteBy = c;
                if (keepQuote) {
                    sb.append(c);
                }
            }
            // 其他，计入
            else {
                sb.append(c);
            }
        }

        // 添加最后一个
        if (!ignoreBlank || !Strings.isBlank(sb)) {
            String s2 = sb.toString();
            list.add(s2);
        }

        // 返回拆分后的数组
        return list;
    }

    /**
     * 将给定字符串，按照 <code>a="xx" b c=xx d='dsd'</code>格式拆分
     * 
     * @param input
     *            输入字符串
     * @return 明值对
     */
    public static NutMap splitAttrMap(String input) {
        NutMap map = new NutMap();
        if (null != input && input.length() > 0) {
            List<String> list = splitQuote(input, "'\"", '\\', true, false, " \t\n");
            for (String li : list) {
                int pos = li.indexOf('=');
                String key = li;
                String val = null;
                if (pos > 0) {
                    key = li.substring(0, pos);
                    val = Ws.trim(li.substring(pos + 1));
                }
                map.put(key, val);
            }
        }
        return map;
    }

    /**
     * 去掉字符串前后空白字符。空白字符的定义由Character.isWhitespace来判断
     *
     * @param cs
     *            字符串
     * @return 去掉了前后空白字符的新字符串
     */
    public static String trim(CharSequence cs) {
        if (null == cs)
            return null;
        int length = cs.length();
        if (length == 0)
            return cs.toString();
        int l = 0;
        int last = length - 1;
        int r = last;
        for (; l < length; l++) {
            if (!Character.isWhitespace(cs.charAt(l)))
                break;
        }
        for (; r > l; r--) {
            if (!Character.isWhitespace(cs.charAt(r)))
                break;
        }
        if (l > r)
            return "";
        else if (l == 0 && r == last)
            return cs.toString();
        return cs.subSequence(l, r + 1).toString();
    }

    public static String trimStart(CharSequence cs) {
        if (null == cs)
            return null;
        int length = cs.length();
        if (length == 0)
            return cs.toString();
        int l = 0;
        for (; l < length; l++) {
            if (!Character.isWhitespace(cs.charAt(l)))
                break;
        }
        if ((length - 1) == l)
            return "";
        if (l > 0)
            return cs.subSequence(l, length).toString();
        return cs.toString();
    }

    public static String trimEnd(CharSequence cs) {
        if (null == cs)
            return null;
        int length = cs.length();
        if (length == 0)
            return cs.toString();
        int last = length - 1;
        int r = last;
        for (; r > 0; r--) {
            if (!Character.isWhitespace(cs.charAt(r)))
                break;
        }
        if (0 == r)
            return "";
        if (r == last)
            return cs.toString();
        return cs.subSequence(0, r + 1).toString();
    }

    /**
     * 如果此字符串为 null 或者为空串（""），则返回 true
     *
     * @param cs
     *            字符串
     * @return 如果此字符串为 null 或者为空，则返回 true
     */
    public static boolean isEmpty(CharSequence cs) {
        return null == cs || cs.length() == 0;
    }

    /**
     * 如果此字符串为 null 或者全为空白字符，则返回 true
     *
     * @param cs
     *            字符串
     * @return 如果此字符串为 null 或者全为空白字符，则返回 true
     */
    public static boolean isBlank(CharSequence cs) {
        if (null == cs)
            return true;
        int length = cs.length();
        for (int i = 0; i < length; i++) {
            if (!(Character.isWhitespace(cs.charAt(i))))
                return false;
        }
        return true;
    }

    /**
     * 复制字符串
     *
     * @param cs
     *            字符串
     * @param n
     *            数量
     * @return 新字符串
     */
    public static String repeat(CharSequence cs, int n) {
        if (isEmpty(cs) || n <= 0)
            return "";
        StringBuilder sb = new StringBuilder(cs.length() * n);
        for (int i = 0; i < n; i++)
            sb.append(cs);
        return sb.toString();
    }

    /**
     * 复制字符
     *
     * @param c
     *            字符
     * @param n
     *            数量
     * @return 新字符串
     */
    public static String repeat(char c, int n) {
        if (c == 0 || n < 1)
            return "";
        char[] cs = new char[n];
        Arrays.fill(cs, c);
        return new String(cs);
    }
}
