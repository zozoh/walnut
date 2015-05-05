package org.nutz.walnut.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Atom;
import org.nutz.walnut.impl.box.WnSystem;

/**
 * Walnut 系统的各种帮助函数集合
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class Wn {

    static class _CTX {

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

    public static String appendPath(String base, String rpath) {
        if (null != rpath) {
            // null
            if (null == base) {
                if (rpath.startsWith("/"))
                    return rpath;
                else
                    return "/" + rpath;
            }
            // root
            else if (base.endsWith("/")) {
                if (rpath.startsWith("/"))
                    return base + rpath.substring(1);
                else
                    return base + rpath;
            }
            // 正常
            else {
                if (rpath.startsWith("/"))
                    return base + rpath;
                else
                    return base + "/" + rpath;
            }
        }
        return base == null ? "/" : base;
    }

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
    public static String normalizePath(String ph, WnSystem sys) {
        if (Strings.isBlank(ph))
            return ph;
        if (ph.startsWith("~")) {
            ph = sys.me.home() + ph.substring(1);
        } else if (ph.startsWith("./")) {
            ph = sys.se.envs().getString("PWD", "") + ph.substring(1);
        } else if (!ph.startsWith("/")) {
            ph = sys.se.envs().getString("PWD", "") + "/" + ph;
        }
        return normalizeStr(ph, sys.se.envs());
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

    public static String evalName(String name, String id) {
        Segment seg = Segments.create(name);
        if (seg.hasKey()) {
            Context c = Lang.context();
            c.set("id", id);
            name = seg.render(c).toString();
        }
        return name;
    }

    public static WnContext WC() {
        WnContext wc = Wn._CTX.get();
        if (null == wc) {
            wc = new WnContext();
            _CTX.set(wc);
        }
        return wc;
    }

    public static final String AT_SEID = "SEID";

    public static class Io {

        public static final int R = 1 << 2;

        public static final int W = 1 << 1;

        public static final int X = 1;

        public static final int RW = R | W;

        public static final int RX = R | X;

        public static final int RWX = R | W | X;

        public static final int WX = W | X;

    }

    public static class ROLE {

        public static final int ADMIN = 1;

        public static final int MEMBER = 10;

        public static final int REQUEST = 100;

        public static final int BLOCK = -1;

    }

    public static String genId() {
        return R.UU32();
    }

    public static final String OBJ_META_PREFIX = ".wn_obj_meta_";

    public static final String OBJ_META_RW = "__obj_meta_rw";

    public static String metaPath(String ph) {
        String nm = Files.getName(ph);
        return Files.getParent(ph) + "/" + OBJ_META_PREFIX + nm;
    }

}
