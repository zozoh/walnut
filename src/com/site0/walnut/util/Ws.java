package com.site0.walnut.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.Encoding;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.util.callback.WnStrToken;
import com.site0.walnut.util.callback.WnStrTokenCallback;
import com.site0.walnut.util.callback.WnStrTokenType;
import com.site0.walnut.util.str.WnStrCaseConvertor;
import com.site0.walnut.util.str.WnStrCases;

/**
 * 字符串帮助类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class Ws {

    /**
     * 将一个用 A-Z表示的字符串转换为一个 1Base的整数
     * 
     * @param s
     *            字符串
     * @return 整数 (1Base)
     */
    public static int fromR26Str(String s) {
        int re = 0;
        char[] cs = s.toCharArray();
        for (int i = 0; i < cs.length; i++) {
            int x = cs.length - i - 1;
            char c = cs[x];
            int d = (int) c;
            if (d < 65 || d > 90) {
                throw Er.create("e.s.invalid.r26", s);
            }
            int n = d - 64;

            re += n * Math.pow(26, i);
        }
        return re;
    }

    /**
     * 将一个整数，表示为用 A-Z表示的字符串
     * 
     * @param n
     *            整数 (1Base)
     * @return 大写字母表示的字符串
     */
    public static String toR26Str(int n) {
        StringBuilder sb = new StringBuilder();
        int m = n - 1;
        do {
            int i = m / 26;
            int x = m - i * 26;
            char c = (char) (65 + x);
            sb.insert(0, c);
            m = i - 1;
        } while (m >= 0);
        return sb.toString();
    }

    private static final char[] CN_NC0 = "零一二三四五六七八九".toCharArray();
    private static final char[] CN_NU0 = "个十百千万亿".toCharArray();

    private static final Map<String, Integer> CN_NUMS = new HashMap<>();
    private static final Map<String, Integer> CN_UNIT = new HashMap<>();

    private static final String CN_REGEX = "(([零一二三四五六七八九])|([个十百千万亿]))";
    private static final Pattern CN_N_P = Regex.getPattern(CN_REGEX);

    static {
        CN_NUMS.put("零", 0);
        CN_NUMS.put("一", 1);
        CN_NUMS.put("二", 2);
        CN_NUMS.put("三", 3);
        CN_NUMS.put("四", 4);
        CN_NUMS.put("五", 5);
        CN_NUMS.put("六", 6);
        CN_NUMS.put("七", 7);
        CN_NUMS.put("八", 8);
        CN_NUMS.put("九", 9);
        CN_UNIT.put("个", 1);
        CN_UNIT.put("十", 10);
        CN_UNIT.put("百", 100);
        CN_UNIT.put("千", 1000);
        CN_UNIT.put("万", 10000);
        CN_UNIT.put("亿", 100000000);
    }

    /**
     * 将一个整数转成中文数字
     * 
     * @param input
     *            整数
     * @return 中文数字
     */
    public static String intToChineseNumber(int input) {
        StringBuilder re = new StringBuilder();

        // 考虑负数
        if (input < 0) {
            re.append('负');
            input *= -1;
        }

        // 优化零
        if (input == 0) {
            re.append(CN_NC0[0]);
            return re.toString();
        }

        // 直接就是个位数
        if (input < 10) {
            char c = CN_NC0[input];
            re.append(c);
            return re.toString();
        }

        // 准备拆分各个位，数组 0 表示个位
        int[] ns = new int[10];
        int len = 0;

        // 挨个来
        int n = input;
        while (n > 0) {
            int nd = n / 10;
            ns[len++] = n - nd * 10;
            n = nd;
        }
        int lastNSIndex = len - 1;

        // 现在我们有一个数字数组
        // [2][3][0][9] ...
        // 个 十 百 千 ...
        int lastN;
        int maxI;
        int lastI;
        //
        // 分作三段输出
        //
        // ................................
        // 亿位段
        if (len > 8) {
            maxI = Math.min(lastNSIndex, 11);
            lastN = -1;
            for (int i = maxI; i >= 8; i--) {
                n = ns[i];
                // 不能输出零零
                if (n == 0 && lastN <= 0) {
                    continue;
                }
                char s_n = CN_NC0[n];
                re.append(s_n);
                // 单位
                if (i > 8 && (n > 0 || lastN > 0)) {
                    char s_u = CN_NU0[i - 8];
                    re.append(s_u);
                }
                // 记录最后一次输出的数字
                lastN = n;
            }
            // 检查，最后一个字符是 '零' 改成 '亿'
            // 否则加个 '亿'
            lastI = re.length() - 1;
            if (re.charAt(lastI) == CN_NC0[0]) {
                re.setCharAt(lastI, CN_NU0[5]);
            } else {
                re.append(CN_NU0[5]);
            }
        }
        // ................................
        // 万位段
        if (len > 4) {
            maxI = Math.min(lastNSIndex, 7);
            lastN = -1;
            for (int i = maxI; i >= 4; i--) {
                n = ns[i];
                // 不能输出零零
                if (n == 0 && lastN <= 0) {
                    continue;
                }
                char s_n = CN_NC0[n];
                re.append(s_n);
                // 单位
                if (i > 4 && (n > 0 || lastN > 0)) {
                    char s_u = CN_NU0[i - 4];
                    re.append(s_u);
                }
                // 记录最后一次输出的数字
                lastN = n;
            }
            // 检查，最后一个字符是 '零' 改成 '万'
            // 否则加个 '万'
            if (lastN >= 0) {
                lastI = re.length() - 1;
                if (re.charAt(lastI) == CN_NC0[0]) {
                    re.setCharAt(lastI, CN_NU0[4]);
                } else {
                    re.append(CN_NU0[4]);
                }
            }
        }

        // ................................
        // 个位段
        maxI = Math.min(lastNSIndex, 3);
        lastN = -1;
        for (int i = maxI; i >= 0; i--) {
            n = ns[i];
            // 不能输出零零
            if (n == 0 && lastN == 0) {
                continue;
            }
            char s_n = CN_NC0[n];
            // 十一 至 十九
            if (i != 1 || n != 1 || maxI > 1) {
                re.append(s_n);
            }
            // 单位
            if (i > 0 && n > 0) {
                char s_u = CN_NU0[i];
                re.append(s_u);
            }
            // 记录最后一次输出的数字
            lastN = n;
        }

        // 输出前，检查，最后一个字符是 '零' 删掉它
        lastI = re.length() - 1;
        if (re.charAt(lastI) == CN_NC0[0]) {
            re.deleteCharAt(lastI);
        }

        return re.toString();
    }

    /**
     * 将中文数字转换为整数
     * 
     * @param str
     *            中文数字
     * @return 数字
     */
    public static int chineseNumberToInt(String str) {
        // 解决负数问题
        int ne = str.startsWith("负") ? -1 : 1;
        if (ne == -1) {
            str = str.substring(1).trim();
        }

        // 汇总结果
        int[] res = new int[str.length()];
        int len = 0;

        int n = -1; // 数字: -1 表示没有
        int u = -1; // 单位: -1 表示没有

        // 逐个查找
        Matcher m = CN_N_P.matcher(str);
        while (m.find()) {
            // 找到了数字位
            String s_n = m.group(2);
            if (null != s_n) {
                n = CN_NUMS.get(s_n);
                continue;
            }
            // 找到了单位
            String s_u = m.group(3);
            if (null != s_u) {
                u = CN_UNIT.get(s_u);
                // 没有数字 ...
                if (n < 0) {
                    // 中文数字特殊规则，如果以 万/亿结尾，则前面都乘一下
                    if (len > 0) {
                        for (int i = 0; i < len; i++) {
                            res[i] *= u;
                        }
                    }
                    // 就是一个干巴单位，认为是 1
                    else {
                        res[len++] = u;
                    }
                    u = -1;
                }
                // 否则搞一下
                else {
                    res[len++] = u * n;
                    u = -1;
                    n = -1;
                }
            }
        }

        // 最后加上个位
        if (n > 0)
            res[len++] = n;

        // 求和
        int re = 0;
        for (int i = 0; i < len; i++)
            re += res[i];

        return re * ne;
    }

    public static String decodeUnicode(String input) {
        byte[] utf8Bytes = input.getBytes(Encoding.CHARSET_UTF8);
        return new String(utf8Bytes, Encoding.CHARSET_UTF8);
    }

    private static Pattern P_HTML_EN = Pattern.compile("&(#([0-9]+)|([a-z]+));");

    public static String escapeHTML(String s) {
        if (null == s) {
            return s;
        }
        StringBuilder sb = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            case '&':
                sb.append("&amp;");
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String decodeHtmlEntities(String text) {
        if (null == text) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        Matcher m = P_HTML_EN.matcher(text);
        while (m.find()) {
            int s = m.start();
            // 前面
            if (s > pos) {
                sb.append(text.substring(pos, s));
            }
            // 找实体
            String num = m.group(2);
            String ens = m.group(3);
            // 数字实体
            if (!Ws.isBlank(num)) {
                int code = Integer.parseInt(num);
                char c = (char) code;
                sb.append(c);
            }
            // 字符实体
            else {
                String c = entities.get(ens);
                if (null != c) {
                    sb.append(c);
                } else {
                    sb.append(m.group(0));
                }
            }
            // 移动
            pos = m.end();
        }
        // 最后一段
        if (pos < text.length()) {
            sb.append(text.substring(pos));
        }
        // 搞定
        return sb.toString();
    }

    private static Map<String, String> entities = new HashMap<>();

    static {
        entities.put("acute", "´");
        entities.put("copy", "©");
        entities.put("gt", ">");
        entities.put("micro", "µ");
        entities.put("reg", "®");
        entities.put("amp", "&");
        entities.put("deg", "°");
        entities.put("iexcl", "¡");
        entities.put("nbsp", " ");
        entities.put("raquo", "»");
        entities.put("brvbar", "¦");
        entities.put("divide", "÷");
        entities.put("iquest", "¿");
        entities.put("not", "¬");
        entities.put("sect", "§");
        entities.put("bull", "•");
        entities.put("frac12", "½");
        entities.put("laquo", "«");
        entities.put("para", "¶");
        entities.put("uml", "¨");
        entities.put("cedil", "¸");
        entities.put("frac14", "¼");
        entities.put("lt", "<");
        entities.put("plusmn", "±");
        entities.put("times", "×");
        entities.put("cent", "¢");
        entities.put("frac34", "¾");
        entities.put("macr", "¯");
        entities.put("quot", "“");
        entities.put("trade", "™");
        entities.put("euro", "€");
        entities.put("pound", "£");
        entities.put("yen", "¥");
        entities.put("bdquo", "„");
        entities.put("hellip", "…");
        entities.put("middot", "·");
        entities.put("rsaquo", "›");
        entities.put("ordf", "ª");
        entities.put("circ", "ˆ");
        entities.put("ldquo", "“");
        entities.put("mdash", "—");
        entities.put("rsquo", "’");
        entities.put("ordm", "º");
        entities.put("dagger", "†");
        entities.put("lsaquo", "‹");
        entities.put("ndash", "–");
        entities.put("sbquo", "‚");
        entities.put("rdquo", "”");
        entities.put("Dagger", "‡");
        entities.put("lsquo", "‘");
        entities.put("permil", "‰");
        entities.put("tilde", "˜");
        entities.put("asymp", "≈");
        entities.put("frasl", "⁄");
        entities.put("larr", "←");
        entities.put("part", "∂");
        entities.put("spades", "♠");
        entities.put("cap", "∩");
        entities.put("ge", "≥");
        entities.put("le", "≤");
        entities.put("Prime", "″");
        entities.put("sum", "∑");
        entities.put("clubs", "♣");
        entities.put("harr", "↔");
        entities.put("loz", "◊");
        entities.put("prime", "′");
        entities.put("uarr", "↑");
        entities.put("darr", "↓");
        entities.put("hearts", "♥");
        entities.put("minus", "−");
        entities.put("prod", "∏");
        entities.put("zwj", "‍");
        entities.put("diams", "♦");
        entities.put("infin", "∞");
        entities.put("ne", "≠");
        entities.put("radic", "√");
        entities.put("zwnj", "‌");
        entities.put("equiv", "≡");
        entities.put("int", "∫");
        entities.put("oline", "‾");
        entities.put("rarr", "→");
        entities.put("alpha", "α");
        entities.put("eta", "η");
        entities.put("mu", "μ");
        entities.put("pi", "π");
        entities.put("theta", "θ");
        entities.put("beta", "β");
        entities.put("gamma", "γ");
        entities.put("nu", "ν");
        entities.put("psi", "ψ");
        entities.put("upsilon", "υ");
        entities.put("chi", "χ");
        entities.put("iota", "ι");
        entities.put("omega", "ω");
        entities.put("rho", "ρ");
        entities.put("xi", "ξ");
        entities.put("delta", "δ");
        entities.put("kappa", "κ");
        entities.put("omicron", "ο");
        entities.put("sigma", "σ");
        entities.put("zeta", "ζ");
        entities.put("epsilon", "ε");
        entities.put("lambda", "λ");
        entities.put("phi", "φ");
        entities.put("tau", "τ");
        entities.put("Alpha", "Α");
        entities.put("Eta", "Η");
        entities.put("Mu", "Μ");
        entities.put("Pi", "Π");
        entities.put("Theta", "Θ");
        entities.put("Beta", "Β");
        entities.put("Gamma", "Γ");
        entities.put("Nu", "Ν");
        entities.put("Psi", "Ψ");
        entities.put("Upsilon", "Υ");
        entities.put("Chi", "Χ");
        entities.put("Iota", "Ι");
        entities.put("Omega", "Ω");
        entities.put("Rho", "Ρ");
        entities.put("Xi", "Ξ");
        entities.put("Delta", "Δ");
        entities.put("Kappa", "Κ");
        entities.put("Omicron", "Ο");
        entities.put("Sigma", "Σ");
        entities.put("Zeta", "Ζ");
        entities.put("Epsilon", "Ε");
        entities.put("Lambda", "Λ");
        entities.put("Phi", "Φ");
        entities.put("Tau", "Τ");
        entities.put("sigmaf", "ς");
    }

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
    public static <T extends Object> String join(T[] arr, String sep) {
        if (null == arr) {
            return null;
        }
        return join(arr, sep, 0, arr.length);
    }

    /**
     * 将容器内对象合并为一个字符串
     * 
     * @param arr
     *            字符串数组
     * @param sep
     *            分隔符
     * @param off
     *            开始元素下标
     * @return 合并后的字符串
     */
    public static <T extends Object> String join(T[] arr, String sep, int off) {
        return join(arr, sep, off, arr.length - off);
    }

    /**
     * 将容器内对象合并为一个字符串
     * 
     * @param arr
     *            字符串数组
     * @param sep
     *            分隔符
     * @param off
     *            开始元素下标
     * @param len
     *            迭代元素数量
     * @return 合并后的字符串
     */
    public static <T extends Object> String join(T[] arr, String sep, int off, int len) {
        StringBuilder sb = new StringBuilder();
        if (len > 0) {
            int lastI = Math.min(arr.length, off + len);
            for (int i = off; i < lastI; i++) {
                if (i > off)
                    sb.append(sep);
                sb.append(arr[i]);
            }
        }
        return sb.toString();
    }

    public static String join(int[] arr, String sep) {
        return join(arr, sep, 0, arr.length);
    }

    public static String join(int[] arr, String sep, int off, int len) {
        StringBuilder sb = new StringBuilder();
        if (len > 0) {
            int lastI = Math.min(arr.length, off + len);
            for (int i = off; i < lastI; i++) {
                if (i > off)
                    sb.append(sep);
                sb.append(arr[i]);
            }
        }
        return sb.toString();
    }

    public static String join(long[] arr, String sep) {
        return join(arr, sep, 0, arr.length);
    }

    public static String join(long[] arr, String sep, int off, int len) {
        StringBuilder sb = new StringBuilder();
        if (len > 0) {
            int lastI = Math.min(arr.length, off + len);
            for (int i = off; i < lastI; i++) {
                if (i > off)
                    sb.append(sep);
                sb.append(arr[i]);
            }
        }
        return sb.toString();
    }

    /**
     * 将容器内对象合并为一个字符串
     * 
     * @param col
     *            字符串集合
     * @param sep
     *            分隔符
     * @return 合并后的字符串
     */
    public static String join(Collection<?> col, String sep) {
        StringBuilder sb = new StringBuilder();
        if (null != col && !col.isEmpty()) {
            Iterator<?> it = col.iterator();
            sb.append(it.next().toString());

            while (it.hasNext()) {
                sb.append(sep);
                sb.append(it.next().toString());
            }
        }
        return sb.toString();
    }

    /**
     * 相当于<code>splitIgnoreBlank(s, ",", 0)</code>
     * 
     * @see #splitIgnoreBlank(String, String, int)
     */
    public static String[] splitIgnoreBlank(String s) {
        return splitIgnoreBlank(s, ",", 0);
    }

    /**
     * 相当于<code>splitIgnoreBlank(s, regex, 0)</code>
     * 
     * @see #splitIgnoreBlank(String, String, int)
     */
    public static String[] splitIgnoreBlank(String s, String regex) {
        return splitIgnoreBlank(s, regex, 0);
    }

    /**
     * 按照指定的正则表达式和限制条件对输入字符串进行分割操作，在分割后会忽略空白（仅包含空白字符）的子字符串，
     * 对非空白的子字符串去除首尾空白字符后，将其整合到一个新的字符串数组中返回。
     * <p>
     * 此方法首先会检查输入的字符串是否为null，如果是null则直接返回null。接着，
     * 调用{@link String#split(String, int)}方法依据给定的正则表达式和限制条件来分割输入字符串，
     * 得到一个子字符串数组。随后遍历该子字符串数组，通过{@link #isBlank(String)}方法判断每个子字符串是否仅由空白字符构成，
     * 若是则跳过该子字符串，若不是空白字符串，则使用{@link #trim(String)}方法去除其首尾空白字符，并将处理后的子字符串添加到一个列表中。
     * 最后，把列表中的元素转换为一个新的字符串数组并返回，该数组中仅包含经过上述处理后的非空白子字符串。
     *
     * @param s
     *            要进行分割操作的原始字符串，可为null，若为null则直接返回null。
     * @param regex
     *            用于分割的正则表达式，用于指定按照何种模式来拆分字符串。
     * @param limit
     *            分割的限制条件，其含义与{@link String#split(String, int)}方法中的限制参数一致，具体解释如下：
     *            <ul>
     *            <li>如果为正数，模式最多应用limit -
     *            1次，数组长度不大于limit，数组最后一项包含最后一个匹配分隔符之后的所有输入内容。</li>
     *            <li>如果为零，模式将尽可能多地应用，数组可以有任意长度，尾部空字符串将被丢弃。</li>
     *            <li>如果为负数，模式将尽可能多地应用，数组可以有任意长度。</li>
     *            </ul>
     * @return 一个字符串数组，数组中的元素为经过去除首尾空白字符处理后的非空白子字符串，若输入字符串为null，则返回null。
     */
    public static String[] splitIgnoreBlank(String s, String regex, int limit) {
        if (null == s)
            return null;
        String[] ss = s.split(regex, limit);
        List<String> list = new LinkedList<String>();
        for (String st : ss) {
            if (isBlank(st))
                continue;
            list.add(trim(st));
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 相当于<code>splitTrimed(s, ",", 0)</code>
     * 
     * @see #splitTrimed(String, String, int)
     */
    public static String[] splitTrimed(String s) {
        return splitTrimed(s, ",", 0);
    }

    /**
     * 相当于<code>splitTrimed(s, regex, 0)</code>
     * 
     * @see #splitTrimed(String, String, int)
     */
    public static String[] splitTrimed(String s, String regex) {
        return splitTrimed(s, regex, 0);
    }

    /**
     * 对给定的字符串按照指定的正则表达式和限制条件进行分割，并对分割后的每个子字符串去除首尾空白字符。
     * <p>
     * 该方法首先调用{@link String#split(String, int)}方法按照传入的正则表达式和限制条件来分割输入字符串，
     * 然后遍历分割后得到的子字符串数组，使用{@link String#trim()}方法去除每个子字符串的首尾空白字符，
     * 最后返回处理后的子字符串数组。
     *
     * @param s
     *            要进行分割操作的原始字符串
     * @param regex
     *            用于分割的正则表达式，指定按照何种模式来拆分字符串
     * @param limit
     *            分割的限制条件，用于控制分割的次数以及结果数组的长度等情况，其含义与{@link String#split(String, int)}方法中的限制参数一致。
     *            具体来说：
     *            <ul>
     *            <li>如果为正数，模式最多应用limit -
     *            1次，数组长度不大于limit，数组最后一项包含最后一个匹配分隔符之后的所有输入内容。</li>
     *            <li>如果为零，模式将尽可能多地应用，数组可以有任意长度，尾部空字符串将被丢弃。</li>
     *            <li>如果为负数，模式将尽可能多地应用，数组可以有任意长度。</li>
     *            </ul>
     * @return 经过分割并去除首尾空白字符后的子字符串数组
     */
    public static String[] splitTrimed(String s, String regex, int limit) {
        String[] ss = s.split(regex, limit);
        for (int i = 0; i < ss.length; i++) {
            ss[i] = ss[i].trim();
        }
        return ss;
    }

    /**
     * 相当于<code>splitTrimedEmptyAsNull(s, regex, 0)</code>
     * 
     * @see #splitTrimedEmptyAsNull(String, String, int)
     */
    public static String[] splitTrimedEmptyAsNull(String s, String regex) {
        return splitTrimedEmptyAsNull(s, regex, 0);
    }

    /**
     * 按照指定的正则表达式和限制条件对输入字符串进行分割，然后对分割得到的每个子字符串进行首尾空白字符去除操作，
     * 并且将去除空白字符后长度为0的子字符串设置为null，最后返回处理后的字符串数组。
     * <p>
     * 首先调用{@link String#split(String, int)}方法，依据传入的正则表达式和限制条件对输入的字符串进行分割，得到一个子字符串数组。
     * 接着遍历这个子字符串数组，针对每个子字符串使用{@link String#trim()}方法去除其首尾空白字符。之后，判断经过修剪后的子字符串长度是否为0，
     * 如果长度为0，就将该子字符串赋值为null，表示将空字符串（去除空白后无实质内容的情况）以null形式体现。最终返回经过上述处理后的字符串数组。
     *
     * @param s
     *            要进行分割操作的原始字符串。
     * @param regex
     *            用于分割的正则表达式，指定了按照何种模式来拆分字符串。
     * @param limit
     *            分割的限制条件，其含义与{@link String#split(String, int)}方法中的限制参数一致，具体含义如下：
     *            <ul>
     *            <li>如果为正数，模式最多应用limit -
     *            1次，数组长度不大于limit，数组最后一项包含最后一个匹配分隔符之后的所有输入内容。</li>
     *            <li>如果为零，模式将尽可能多地应用，数组可以有任意长度，尾部空字符串将被丢弃。</li>
     *            <li>如果为负数，模式将尽可能多地应用，数组可以有任意长度。</li>
     *            </ul>
     * @return 经过分割、去除首尾空白字符以及将空字符串处理为null后的字符串数组。
     */
    public static String[] splitTrimedEmptyAsNull(String s, String regex, int limit) {
        String[] ss = s.split(regex, limit);
        for (int i = 0; i < ss.length; i++) {
            ss[i] = ss[i].trim();
            if (ss[i].length() == 0) {
                ss[i] = null;
            }
        }
        return ss;
    }

    /**
     * 相当于<code>splitIgnoreBlanks(s, ",", 0)</code>
     * 
     * @see #splitIgnoreBlanks(String, String, int)
     */
    public static List<String> splitIgnoreBlanks(String s) {
        return splitIgnoreBlanks(s, ",");
    }

    /**
     * 相当于<code>splitIgnoreBlanks(s, regex, 0)</code>
     * 
     * @see #splitIgnoreBlanks(String, String, int)
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

    /**
     * 按照指定的正则表达式和限制条件对输入字符串进行分割，忽略分割后为空（空白字符组成）的子字符串，
     * 并对非空的子字符串去除首尾空白字符后添加到列表中返回。
     * <p>
     * 首先会判断输入的字符串是否为null，如果是则直接返回null。然后调用{@link String#split(String, int)}
     * 方法按照给定的正则表达式和限制条件对字符串进行分割，得到一个子字符串数组。接着遍历该数组，
     * 通过{@link #isBlank(String)}方法判断每个子字符串是否为空（仅包含空白字符），如果为空则跳过，
     * 不为空的话使用{@link #trim(String)}方法去除首尾空白字符后添加到返回的列表中。
     *
     * @param s
     *            要进行分割操作的原始字符串，可为null，如果为null则直接返回null。
     * @param regex
     *            用于分割的正则表达式，决定按照何种模式拆分字符串。
     * @param limit
     *            分割的限制条件，其含义与{@link String#split(String, int)}方法中的限制参数一致，具体如下：
     *            <ul>
     *            <li>如果为正数，模式最多应用limit -
     *            1次，数组长度不大于limit，数组最后一项包含最后一个匹配分隔符之后的所有输入内容。</li>
     *            <li>如果为零，模式将尽可能多地应用，数组可以有任意长度，尾部空字符串将被丢弃。</li>
     *            <li>如果为负数，模式将尽可能多地应用，数组可以有任意长度。</li>
     *            </ul>
     * @return 一个包含经过处理后的非空白子字符串的列表，若输入字符串为null则返回null，列表中的每个元素都是去除首尾空白字符后的非空白子字符串。
     */
    public static List<String> splitIgnoreBlanks(String s, String regex, int limit) {
        if (null == s)
            return null;
        String[] ss = s.split(regex, limit);
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
    public static final Wchar.EscapeTable STR_UNESC_TAB = Wchar.buildEscapeTable(STR_ESC_TS);

    // 编码过程表: 真正字符 -> 转义字符串
    public static final Wchar.EscapeTable STR_ESC_TAB = Wchar.buildEscapeReverTable(STR_ESC_TS);

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

    public static char escapeChar(char c) {
        return STR_ESC_TAB.get(c);
    }

    public static char unescapeChar(char c) {
        return STR_UNESC_TAB.get(c);
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

    public static String encodeUrlPair(String key, Object val) {
        try {
            if (null != val) {
                return String.format("%s=%s",
                                     URLEncoder.encode(key, Encoding.UTF8),
                                     URLEncoder.encode(val.toString(), Encoding.UTF8));
            } else {
                return URLEncoder.encode(key, Encoding.UTF8);
            }
        }
        catch (UnsupportedEncodingException e) {
            throw Er.wrap(e);
        }
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
                    throw Wlang.makeThrow("evalEscape invalid char[%d] '%c'  : %s", i, c, str);
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

    public static List<String> splitQuote(String str) {
        return splitQuote(str, true);
    }

    public static List<String> splitQuote(String str, boolean keepQuote) {
        return splitQuote(str, false, keepQuote);
    }

    public static List<String> splitQuote(String str, boolean ignoreBlank, boolean keepQuote) {
        return splitQuote(str, "\"'", '\\', ignoreBlank, keepQuote, ",;、，； \t\r\n");
    }

    /**
     * 将字符串按照某个或几个分隔符拆分。 其中，遇到字符串 "xxx" 或者 'xxx' 并不拆分
     *
     * @param str
     *            要被拆分的字符串
     * @param quote
     *            支持的引号字符集合
     * @param escapeChar
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
                                          char escapeChar,
                                          boolean ignoreBlank,
                                          boolean keepQuote,
                                          String seps) {
        char[] qucs = quote.toCharArray();
        char[] spcs = seps.toCharArray();
        List<String> list = new LinkedList<String>();
        char[] cs = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        char quoteBy = 0;
        for (int i = 0; i < cs.length; i++) {
            char c = cs[i];
            // 引用外，且遇到分隔符号就拆分
            if (0 == quoteBy && Wchar.isIn(spcs, c)) {
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
                // 逃逸字符
                if (escapeChar == c) {
                    sb.append(c);
                    if (i < cs.length) {
                        c = cs[++i];
                        sb.append(c);
                    }
                    continue;
                }
                // 结束引用
                if (quoteBy == c) {
                    if (keepQuote) {
                        sb.append(c);
                    }
                    quoteBy = 0;
                    continue;
                }
                // 那么就是引用咯
                sb.append(c);
                continue;
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
        if (!ignoreBlank || !isBlank(sb)) {
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
        List<String> stack = new LinkedList<>();
        StringBuilder sb = new StringBuilder();
        Ws.splitQuoteToken(input, "\"'", " \t=", new WnStrTokenCallback() {
            public void invoke(WnStrToken token) {
                switch (token.type) {
                // 引号
                case QUOTE:
                    if (sb.length() > 0) {
                        stack.add(sb.toString());
                        sb.delete(0, sb.length());
                    }
                    // 重置
                    stack.add(token.text.toString());
                    break;
                // 普通文字
                case TEXT:
                    sb.append(token.text);
                    break;
                // 连续的引号
                // 分隔符
                // 会导致开启一个新项
                case SEPERATOR:
                    char c = token.src[token.index];
                    if ('=' == c) {
                        if (sb.length() > 0) {
                            stack.add(sb.toString());
                            sb.delete(0, sb.length());
                        }
                    }
                    // 一个属性结束
                    else {
                        // 处理最后一个
                        if (sb.length() > 0) {
                            stack.add(sb.toString());
                            sb.delete(0, sb.length());
                        }
                        // 只有名称
                        if (stack.size() == 1) {
                            String name = stack.remove(0);
                            map.put(name, null);
                        }
                        // 有名称和值
                        else if (stack.size() > 1) {
                            String name = stack.remove(0);
                            String val = stack.remove(0);
                            map.put(name, val);
                        }
                        stack.clear();
                    }
                    break;
                // 不可能
                default:
                    throw Wlang.impossible();
                }
            }

            @Override
            public char escape(char c) {
                return 0;
            }
        });
        // 处理最后一个
        if (sb.length() > 0) {
            stack.add(sb.toString());
        }
        // 只有名称
        if (stack.size() == 1) {
            String name = stack.remove(0);
            map.put(name, null);
        }
        // 有名称和值
        else if (stack.size() > 1) {
            String name = stack.remove(0);
            String val = stack.remove(0);
            map.put(name, val);
        }

        return map;
    }

    public static void splitQuoteToken(String input,
                                       String quotes,
                                       String seperators,
                                       WnStrTokenCallback callback) {
        char[] qs = null == quotes ? null : quotes.toCharArray();
        char[] ss = null == seperators ? null : seperators.toCharArray();
        splitQuoteToken(input, qs, ss, callback);
    }

    /**
     * 根据引号逐个回调输入字符串。调用回调的类型详情清参看:
     * <p>
     * 回调函数的参数，请查看 <code>WnStrTokenType</code> 的描述
     * 
     * @param input
     *            输入字符串
     * @param quotes
     *            哪些字符是引号，譬如 <code>'"`</code>
     * @param seperators
     *            哪些字符是分隔符，譬如 <code>\t\s\r\n</code>
     * @param callback
     *            回调
     * 
     * @see org.nutz.walnut.util.callback.WnStrTokenType
     */
    public static void splitQuoteToken(String input,
                                       char[] quotes,
                                       char[] seperators,
                                       WnStrTokenCallback callback) {
        if (null == input || null == quotes || quotes.length == 0 || null == callback) {
            return;
        }

        char[] cs = input.toCharArray();

        // 依次循环
        WnStrToken tk = new WnStrToken();
        tk.src = cs;
        tk.index = 0;
        tk.quoteC = 0;
        tk.text = new StringBuilder();
        // tk.offset = 0;
        int lastI = cs.length - 1;

        // 循环字符串
        for (; tk.index < cs.length; tk.index++) {
            char c = cs[tk.index];

            // 尝试逃逸
            if (c == '\\') {
                // 找到逃逸字符
                if (tk.index < lastI) {
                    tk.index++;
                    char c2 = cs[tk.index];
                    char c3 = callback.escape(c2);
                    // 逃逸成功
                    if (c3 != 0) {
                        tk.text.append(c3);
                        continue;
                    }
                    // 逃逸失败，回退
                    else {
                        tk.index--;
                    }
                }
            }

            // 已经在引号里了
            if (tk.quoteC > 0) {
                // 遇到引号结束
                if (tk.quoteC == c) {
                    tk.type = WnStrTokenType.QUOTE;
                    callback.invoke(tk);
                    tk.reset((char) 0);
                }
                // 那么当作普通字符串哦
                else {
                    tk.text.append(c);
                }
            }
            // 是否遇到了引号符
            else if (tk.index < lastI && Wchar.indexOf(quotes, c) >= 0) {
                // 看看之前有没有内容
                if (tk.hasText()) {
                    tk.type = WnStrTokenType.TEXT;
                    callback.invoke(tk);
                    tk.reset((char) 0);
                }
                // 标记一下进入引号
                tk.reset(c);
            }
            // 是否遇到了分隔符
            else if (Wchar.indexOf(seperators, c) >= 0) {
                // 看看之前有没有内容
                if (tk.hasText()) {
                    tk.type = WnStrTokenType.TEXT;
                    callback.invoke(tk);
                    tk.reset((char) 0);
                }
                // 调用分隔符
                tk.type = WnStrTokenType.SEPERATOR;
                tk.quoteC = c;
                callback.invoke(tk);
                tk.reset((char) 0);
            }
            // 否则就记入
            else {
                tk.text.append(c);
            }
        }

        // 最后一部分
        if (tk.text.length() > 0) {
            tk.index = cs.length;
            tk.type = WnStrTokenType.TEXT;
            callback.invoke(tk);
        }
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
            char c = cs.charAt(l);
            if (!Character.isWhitespace(c) && 160 != c)
                break;
        }
        for (; r > l; r--) {
            char c = cs.charAt(r);
            if (!Character.isWhitespace(c) && 160 != c)
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
     * 对指定对象进行 toString 操作；如果该对象为 null ，则返回空串（""）
     *
     * @param obj
     *            指定的对象
     * @return 对指定对象进行 toString 操作；如果该对象为 null ，则返回空串（""）
     */
    public static String sBlank(Object obj) {
        return sBlank(obj, "");
    }

    /**
     * 对指定对象进行 toString 操作；如果该对象为 null 或者 toString 方法为空串（""），则返回默认值
     *
     * @param obj
     *            指定的对象
     * @param def
     *            默认值
     * @return 对指定对象进行 toString 操作；如果该对象为 null 或者 toString 方法为空串（""），则返回默认值
     */
    public static String sBlank(Object obj, String def) {
        if (null == obj)
            return def;
        String s = obj.toString();
        return isBlank(s) ? def : s;
    }

    public static String sBlanks(Object... objs) {
        for (Object obj : objs) {
            if (null == obj) {
                continue;
            }
            if (obj instanceof CharSequence) {
                if (Ws.isBlank(obj.toString())) {
                    continue;
                }
            }
            return obj.toString();
        }
        return null;
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

    private static Pattern JV_NB = Pattern.compile("^(-?([0-9]+)([.][0-9]+)?)([Lf])?$");

    /**
     * 根据字符串，将其自动转成一个有意义的值
     * 
     * <ul>
     * <li><code>"null|undefined"</code> : <code>null</code>
     * <li><code>"(true|false|yes|no|on|off)"</code> : 布尔
     * <li><code>"(0-9)"</code> : 整数
     * <li><code>"{..}"</code> : Map
     * <li><code>"[..]"</code> : 列表
     * <li><code>"2020-09.."</code> : 日期时间
     * </ul>
     * 
     * @param v
     *            输入字符串
     * @return 值
     */
    public static Object toJavaValue(String v) {
        // autoNil
        if (null == v || "null".equals(v) || "undefined".equals(v))
            return null;
        /**
         * Number
         * 
         * <pre>
             0:[  0,  6) `23.46f`
             1:[  0,  5) `23.46`
             2:[  0,  2) `23`
             3:[  2,  5) `.46`
             4:[  5,  6) `f`
             
             0:[  0,  5) `-200L`
             1:[  0,  4) `-200`
             2:[  1,  4) `200`
             3:[ -1, -1) `null`
             4:[  4,  5) `L`
         * </pre>
         */
        Matcher m = JV_NB.matcher(v);
        if (m.find()) {
            String n = m.group(1);
            String suffix = m.group(4);
            if ("f".equals(suffix)) {
                return Float.parseFloat(n);
            }
            if ("L".equals(suffix)) {
                return Long.parseLong(n);
            }
            if (m.group(3) != null) {
                return Double.parseDouble(n);
            }
            return Integer.parseInt(n);
        }

        // Boolean
        if (v.matches("^(true|false|yes|no|on|off)$")) {
            return v.matches("^(true|yes|on)$");
        }

        // JS String
        if (Ws.isQuoteBy(v, '\'', '\'') || Ws.isQuoteBy(v, '"', '"')) {
            return v.substring(1, v.length() - 2);
        }

        try {
            // JSON
            if (Ws.isQuoteBy(v, '[', ']') || Ws.isQuoteBy(v, '{', '}')) {
                return Json.fromJson(v);
            }
            // Date
            return Times.D(v);
        }
        catch (Exception e) {}

        // 最后返回自己（尝试逃逸）
        return Ws.unescape(v);
    }

    /**
     * 将字符串改为 SnakeCase
     * 
     * @param cs
     *            输入
     * @return 输入 SnakeCase 字符串
     */
    public static String snakeCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        List<String> words = splitWords(input, false, false);
        if (words.isEmpty()) {
            return "";
        }

        return Ws.join(words, "_");
    }

    /**
     * 将字符串改为 KebabCase
     * 
     * @param cs
     *            输入
     * @return 输入 KebabCase 字符串
     */
    public static String kebabCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        List<String> words = splitWords(input, false, false);
        if (words.isEmpty()) {
            return "";
        }

        return Ws.join(words, "-");
    }

    /**
     * 将字符串改为 HeaderCase。 即，kebab与Camel的混合体
     * 
     * @param cs
     *            输入
     * @return 输入 HeaderCase 字符串
     */
    public static String headerCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        List<String> words = splitWords(input, true, true);
        if (words.isEmpty()) {
            return "";
        }

        return Ws.join(words, "-");
    }

    /**
     * 将字符串改为 CamelCase
     * 
     * @param cs
     *            输入
     * @return 输入 CamelCase 字符串
     */
    public static String camelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        List<String> words = splitWords(input, false, true);

        if (words.isEmpty()) {
            return "";
        }

        return Ws.join(words, "");
    }

    /**
     * 将输入的字符串拆分为单词列表
     * 
     * @param input
     *            输入字符串
     * @param cfl_0
     *            第一个单词需要首字母大写
     * @param cfl_n
     *            后续单词需要首字母大写
     * @return 拆分的单词
     */
    public static List<String> splitWords(String input, boolean cfl_0, boolean cfl_n) {
        char[] chars = input.trim().toCharArray();
        List<String> words = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        // 第一个字母
        char c = chars[0];
        int word_type = Character.getType(c);
        if (c > 128 || word_type <= 11) {
            if (cfl_0) {
                sb.append(Character.toUpperCase(c));
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }

        for (int i = 1; i < chars.length; i++) {
            c = chars[i];
            int type = Character.getType(c);
            // 相同的单词
            // 1. 相同的类型
            // 2. 首字母大写，后续小写也能接受
            if (word_type == type
                || (Character.UPPERCASE_LETTER == word_type
                    && Character.LOWERCASE_LETTER == type)) {
                if (c > 128 || type <= 11) {
                    if (cfl_n && sb.length() == 0) {
                        sb.append(Character.toUpperCase(c));
                    } else {
                        sb.append(Character.toLowerCase(c));
                    }
                }
            }
            // 那么就分隔单词
            else {
                if (sb.length() > 0) {
                    String w = sb.toString();
                    words.add(w);
                    sb.setLength(0);
                }
                if (c > 128 || type <= 11) {
                    if (cfl_n) {
                        sb.append(Character.toUpperCase(c));
                    } else {
                        sb.append(Character.toLowerCase(c));
                    }
                }
            }
            // 更新当前的类型
            word_type = type;
        }

        // 添加最后一个单词
        if (sb.length() > 0) {
            words.add(sb.toString());
        }
        return words;
    }

    public static String toUpper(String input) {
        if (null == input) {
            return null;
        }
        return input.toUpperCase();
    }

    public static String toLower(String input) {
        if (null == input) {
            return null;
        }
        return input.toLowerCase();
    }

    public static String toCase(String input, String mode) {
        WnStrCaseConvertor cc = WnStrCases.check(mode);
        return cc.covert(input);
    }

    public static String upperFirst(String input) {
        if (null == input || 0 == input.length()) {
            return input;
        }
        char[] cs = input.toCharArray();
        cs[0] = Character.toUpperCase(cs[0]);
        return new String(cs);
    }

    public static String lowerFirst(String input) {
        if (null == input || 0 == input.length()) {
            return input;
        }
        char[] cs = input.toCharArray();
        cs[0] = Character.toLowerCase(cs[0]);
        return new String(cs);
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

    /**
     * 在字符串右侧填充一定数量的特殊字符
     * 
     * <pre>
     * padEnd("a", 4, '0') => "a000"
     * </pre>
     *
     * @param s
     *            字符串
     * @param width
     *            字符数量
     * @param c
     *            字符
     * @return 新字符串
     */
    public static String padEnd(String s, int width, char c) {
        if (null == s)
            return null;
        int len = s.length();
        if (len >= width)
            return s.toString();
        StringBuilder re = new StringBuilder();
        re.append(s);
        re.append(repeat(c, width - len));
        return re.toString();
    }

    /**
     * 在字符串左侧填充一定数量的特殊字符
     * 
     * <pre>
     * padStart("a", 4, '0') => "000a"
     * </pre>
     *
     * @param s
     *            字符串
     * @param width
     *            字符数量
     * @param c
     *            字符
     * @return 新字符串
     */
    public static String padStart(String s, int width, char c) {
        if (null == s)
            return null;
        int length = s.length();
        if (length >= width)
            return s;
        StringBuilder re = new StringBuilder();
        re.append(repeat(c, width - length));
        re.append(s);
        return re.toString();
    }

    public static String formatAms(long ams, String format) {
        Date d = new Date(ams);
        return Times.format("yyyy-MM-dd HH:mm:ss", d);
    }

    public static String formatAms(long ams) {
        return formatAms(ams, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * @param len
     *            字节长度
     * @return 比较容易阅读的字节长度表示方式
     */
    public static String sizeText(long len) {
        return sizeText(len, true);
    }

    /**
     * @param len
     *            字节长度
     * @param bytes
     *            是否输出实际字节
     * @return 比较容易阅读的字节长度表示方式
     */
    public static String sizeText(long len, boolean bytes) {
        int fixed = 2;
        double M = 1024;
        String[] units = Wlang.array("Bytes", "KB", "MB", "GB", "PB", "TB");
        double nb = len;
        int i = 0;
        for (; i < units.length; i++) {
            double nb2 = nb / M;
            if (nb2 < 1) {
                break;
            }
            nb = nb2;
        }
        String unit = units[i];
        String re = "";
        if (nb == Math.ceil(nb)) {
            re = ((long) nb + unit);
        } else {
            nb = Wnum.precise(nb, fixed);
            re = nb + unit;
        }

        if (bytes && i > 0) {
            return re + "(" + len + "bytes)";
        }
        return re;
    }

}
