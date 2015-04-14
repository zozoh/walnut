package org.nutz.walnut.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Maths;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Atom;

/**
 * Walnut 系统的各种帮助函数集合
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class Wn {

    public static class Context {

        private static ThreadLocal<WnContext> _wn_context = new ThreadLocal<WnContext>();

        public static void set(WnContext wc) {
            _wn_context.set(wc);
        }

        public static WnContext get() {
            return _wn_context.get();
        }

        public static void clear() {
            _wn_context.set(null);
        }

        public static void wrap(WnContext wc, Atom atom) {
            WnContext old = get();
            try {
                set(wc);
                atom.run();
            }
            finally {
                set(old);
            }
        }

    }

    private static final String regex = "([$])([a-zA-Z0-9_]+)";
    private static final Pattern p = Pattern.compile(regex);

    public static Pattern wildcardToRegex(String wildcard) {
        if (null == wildcard)
            return null;
        // TODO zozoh: 应该考虑一些特殊字符 ...
        String s = wildcard.replace("*", ".*");
        return Pattern.compile("^" + s + "$");
    }

    public static boolean matchWildcard(String s, String wildcard) {
        Pattern p = wildcardToRegex(wildcard);
        if (null == p)
            return false;
        return p.matcher(s).matches();
    }

    /**
     * 根据给定的环境变量，整理字符串
     * 
     * @param ph
     *            字符串
     * @param env
     *            环境变量
     * @return 整理后的字符串
     */
    public static String normalizePath(String ph, NutMap env) {
        if (Strings.isBlank(ph))
            return ph;
        if (ph.startsWith("~")) {
            ph = env.getString("HOME", "") + ph.substring(1);
        } else if (ph.startsWith("./")) {
            ph = env.getString("PWD", "") + ph.substring(1);
        } else if (!ph.startsWith("/")) {
            ph = env.getString("PWD", "") + "/" + ph;
        }
        return normalizeStr(ph, env);
    }

    public static String normalizeStr(String str, NutMap env) {
        Matcher m = p.matcher(str);
        int pos = 0;
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            // System.out.println(Dumps.matcherFound(m));
            int l = m.start();
            int r = m.end();
            if (l > pos) {
                sb.append(str.substring(pos, l));
            }
            pos = r;
            String var = m.group(2);
            String val = env.getString(var, "");
            val = normalizeStr(val, env);
            sb.append(val);
        }
        if (pos == 0)
            return str;
        if (pos < str.length())
            sb.append(str.substring(pos));
        return sb.toString();
    }

    public static WnContext WC() {
        WnContext wc = Wn.Context.get();
        if (null == wc) {
            wc = new WnContext();
            Context.set(wc);
        }
        return wc;
    }

    public static final String AT_SEID = "SEID";

    public static class RM {

        public static final int NONE = 0;

        public static final int FORCE = 1;

        public static final int RECUR = 1 << 1;

        public static boolean isRecur(int m) {
            return Maths.isMask(m, RECUR);
        }

        public static boolean isForce(int m) {
            return Maths.isMask(m, FORCE);
        }

    }

    public static class MK {

        public static final int NONE = 0;

        public static final int NO_MAKE_PARENTS = 1;

        public static final int MAKE_IF_NOEXISTS = 1 << 1;

        // 如果没有开启这个选项，则根据对象的 RACE 和 类型来自动决定持久化设置
        public static final int NO_PERSIST = 1 << 2;

        public static boolean isNoMakeParent(int m) {
            return Maths.isMask(m, NO_MAKE_PARENTS);
        }

        public static boolean isMakeIfNoExists(int m) {
            return Maths.isMask(m, MAKE_IF_NOEXISTS);
        }

        public static boolean isNoPersist(int m) {
            return Maths.isMask(m, NO_PERSIST);
        }

    }

    public static class Io {

        public static final int S = 1;

        public static final int R = 1 << 1;

        public static final int A = 1 << 2;

        public static final int W = 1 << 3;

        // public static final int SR = R | 1;
        //
        public static final int SRA = R | A | S;

        public static final int RW = R | W;

        public static final int RA = R | A;

        public static boolean isR(int m) {
            return Maths.isMask(m, R);
        }

        public static boolean isW(int m) {
            return Maths.isMask(m, W);
        }

        public static boolean isAW(int m) {
            return Maths.isMask(m, A | W);
        }

        public static boolean isA(int m) {
            return Maths.isMask(m, A);
        }

        public static boolean isS(int m) {
            return Maths.isMask(m, S);
        }

        public static boolean isSA(int m) {
            return Maths.isMask(m, S | A);
        }

    }

    public static final String BLOCK_ENDL = "#~END";

    public static String genId() {
        return R.UU32();
    }

}
