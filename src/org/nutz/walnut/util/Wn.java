package org.nutz.walnut.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Maths;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.web.Webs.Err;

/**
 * Walnut 系统的各种帮助函数集合
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class Wn {

    // public static void main(String[] args) throws InterruptedException {
    // for (int i = 0; i < 10; i++) {
    // System.out.println(" N:" + System.nanoTime());
    // System.out.println("ms:" + System.currentTimeMillis());
    // System.out.println("ns:" + Wn.nanoTime());
    // System.out.println("---------------------------------");
    // Thread.sleep(100);
    // }
    // }

    public static class Ctx {

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

    public static WnObj getObj(WnSystem sys, String str) {
        return getObj(sys.io, sys.se, str);

    }

    public static WnObj getObj(WnIo io, WnSession se, String str) {
        // 用 ID
        if (str.startsWith("id:")) {
            String id = str.substring("id:".length());
            return io.get(id);
        }

        // 用路径
        String path = normalizeFullPath(str, se);
        return io.fetch(null, path);

    }

    public static WnObj getObj(WnIo io, String str) {
        if (Strings.isBlank(str))
            return null;

        if (str.startsWith("id:")) {
            String id = str.substring("id:".length());
            return io.get(id);
        }

        return io.fetch(null, str);
    }

    public static WnObj checkObj(WnIo io, String str) {
        WnObj o = getObj(io, str);
        if (null == o)
            throw Er.create("e.io.obj.noexists", str);
        return o;
    }

    public static WnObj checkObj(WnSystem sys, String str) {
        return checkObj(sys.io, sys.se, str);

    }

    public static WnObj checkObj(WnIo io, WnSession se, String str) {
        WnObj o = getObj(io, se, str);
        if (null == o)
            throw Er.create("e.io.obj.noexists", str);
        return o;
    }

    private static final String regex = "([$])([a-zA-Z0-9_]+)";
    private static final Pattern p = Pattern.compile(regex);

    // public static String appendPath(String base, String rpath) {
    // if (!Strings.isBlank(rpath)) {
    // // null
    // if (null == base) {
    // if (rpath.startsWith("/"))
    // return rpath;
    // else
    // return "/" + rpath;
    // }
    // // root
    // else if (base.endsWith("/")) {
    // if (rpath.startsWith("/"))
    // return base + rpath.substring(1);
    // else
    // return base + rpath;
    // }
    // // 正常
    // else {
    // if (rpath.startsWith("/"))
    // return base + rpath;
    // else
    // return base + "/" + rpath;
    // }
    // }
    // return base == null ? "/" : base;
    // }

    public static String appendPath(String... phs) {
        String[] paths = Lang.without(phs, null);
        if (null != paths && paths.length > 0) {
            // zozoh: 嗯下面的逻辑木有必要了吧
            // if (null == paths[0])
            // paths[0] = "/";
            String str = Lang.concat("/", paths).toString();
            String[] ss = Strings.splitIgnoreBlank(str, "/");
            str = Lang.concat("/", ss).toString();
            if (paths[0].startsWith("/")) {
                return "/" + str;
            }
            return str;
        }
        return null;
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

    public static String getUsrHome(String usr) {
        return "root".equals(usr) ? "/root" : "/home/" + usr;
    }

    public static String normalizePath(String ph, WnSystem sys) {
        return normalizePath(ph, sys.se);
    }

    public static String normalizePath(String ph, WnSession se) {
        if (Strings.isBlank(ph))
            return ph;
        // 主目录开头
        if (ph.startsWith("~")) {
            ph = se.vars().getString("HOME") + ph.substring(1);
        }
        // 当前目录开头
        else if (ph.startsWith("./")) {
            ph = se.vars().getString("PWD", "") + ph.substring(1);
        }

        return normalizeStr(ph, se.vars());
    }

    public static String normalizeFullPath(String ph, WnSystem sys) {
        return normalizeFullPath(ph, sys.se);
    }

    public static String normalizeFullPath(String ph, WnSession se) {
        if (Strings.isBlank(ph))
            return ph;
        String path = normalizePath(ph, se);
        String pwd = se.vars().getString("PWD", "");

        String re;

        // 如果指明就是当前路径
        if (".".equals(path)) {
            re = pwd;
        }
        // 稍微复杂点的路径
        else {
            if (path.endsWith("/.")) {
                re = path.substring(0, path.length() - 2);
            } else {
                re = path;
            }

            // 组合上当前目录
            if (!path.startsWith("/")) {
                re = Wn.appendPath(pwd, path);
            }
        }

        return re;
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
        WnContext wc = Wn.Ctx.get();
        if (null == wc) {
            wc = new WnContext();
            Ctx.set(wc);
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

        public static String modeToStr(int md) {
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i >= 0; i--) {
                int m = md >> (i * 3) & RWX;
                sb.append((m & R) > 0 ? 'r' : '-');
                sb.append((m & W) > 0 ? 'w' : '-');
                sb.append((m & X) > 0 ? 'x' : '-');
            }
            return sb.toString();
        }

        public static int modeFromStr(String mds) {
            int md = 0;
            for (int i = 0; i < 3; i++) {
                int m = 0;
                int left = (2 - i) * 3;
                char[] cs = mds.substring(left, left + 3).toCharArray();
                if (cs[0] == 'r')
                    m |= R;
                if (cs[1] == 'w')
                    m |= W;
                if (cs[2] == 'x')
                    m |= X;
                md |= m << (i * 3);
            }
            return md;
        }

        /**
         * 计算对象的 d0,d1，并填充字段
         * 
         * @param o
         *            对象
         * @return 对象路径数组
         */
        public static String[] eval_dn(WnObj o) {
            String ph = o.path();
            String[] ss = Strings.splitIgnoreBlank(ph, "/");
            for (int i = 0; i < 2; i++) {
                if (i < ss.length)
                    o.put("d" + i, ss[i]);
                else
                    o.put("d" + i, null);
            }
            return ss;
        }

        /**
         * 修改一个对象所有祖先的同步时间。当然，未设置同步的祖先会被无视
         * 
         * @param tree
         *            元数据读写接口
         * @param o
         *            对象
         * @param includeSelf
         *            是否也检视自身的同步时间
         */
        public static void update_ancestor_synctime(final WnTree tree,
                                                    final WnObj o,
                                                    final boolean includeSelf) {
            WnContext wc = Wn.WC();

            // 防止无穷递归
            if (wc.isSynctimeOff())
                return;

            final List<WnObj> list = new LinkedList<WnObj>();
            o.loadParents(list, false);
            final long synctime = System.currentTimeMillis();
            wc.synctimeOff(new Atom() {
                public void run() {
                    for (WnObj an : list) {
                        if (an.syncTime() > 0) {
                            an.syncTime(synctime);
                            tree.set(an, "^st$");
                        }
                    }
                    if (includeSelf && o.syncTime() > 0) {
                        o.syncTime(synctime);
                        tree.set(o, "^st$");
                    }
                }
            });
        }

    }

    public static class ROLE {

        public static final int OTHERS = 0;

        public static final int ADMIN = 1;

        public static final int MEMBER = 10;

        public static final int REQUEST = 100;

        public static final int BLOCK = -1;

        public static String getRoleName(int role) {
            switch (role) {
            case Wn.ROLE.ADMIN:
                return "ADMIN";
            case Wn.ROLE.BLOCK:
                return "BLOCK";
            case Wn.ROLE.MEMBER:
                return "MEMEBER";
            case Wn.ROLE.OTHERS:
                return "OTHERS";
            case Wn.ROLE.REQUEST:
                return "REQUEST";
            default:
                throw Er.create("e.io.role.invalid", role);
            }
        }

        public static int getRoleValue(String roleName) {
            String rn = roleName.toUpperCase();
            if ("ADMIN".equals(rn))
                return ADMIN;
            if ("BLOCK".equals(rn))
                return BLOCK;
            if ("MEMBER".equals(rn))
                return MEMBER;
            if ("OTHERS".equals(rn))
                return OTHERS;
            if ("REQUEST".equals(rn))
                return REQUEST;
            throw Er.create("e.io.role.invalid", roleName);
        }

    }

    public static class S {

        /**
         * 追加模式，句柄不可 seek，只会向后面添加内容
         */
        public static final int A = 1;
        /**
         * 写模式，可以和修改模式(M)混用
         */
        public static final int W = 1 << 1;
        /**
         * 读模式
         */
        public static final int R = 1 << 2;
        /**
         * 修改模式，如果非修改模式，关闭句柄，会删除后面的内容
         */
        public static final int M = 1 << 3;
        /**
         * 读写模式
         */
        public static final int RW = R | W;
        public static final int WM = W | M;

        public static boolean isRead(int mode) {
            return Maths.isMask(mode, R);
        }

        public static boolean isReadOnly(int mode) {
            return Maths.isMaskAll(mode, R);
        }

        public static boolean isWite(int mode) {
            return Maths.isMask(mode, W);
        }

        public static boolean isModify(int mode) {
            return Maths.isMask(mode, M);
        }

        public static boolean isWriteOnly(int mode) {
            return Maths.isMaskAll(mode, W);
        }

        public static boolean isAppend(int mode) {
            return Maths.isMask(mode, A);
        }

        public static boolean isWriteOrAppend(int mode) {
            return Maths.isMask(mode, W | A);
        }

        public static boolean isReadWrite(int mode) {
            return Maths.isMask(mode, RW);
        }
    }

    public static String genId() {
        return R.UU32();
    }

    public static class Q {

        public static WnQuery id(WnObj o) {
            return id(o.id());
        }

        public static WnQuery id(String id) {
            return new WnQuery().setv("id", id);
        }

        public static WnQuery pid(WnObj p) {
            return pid(p.id());
        }

        public static WnQuery pid(String pid) {
            return new WnQuery().setv("pid", pid);
        }

    }

    public static final String OBJ_META_PREFIX = ".wn_obj_meta_";

    public static String metaPath(String ph) {
        String nm = Files.getName(ph);
        return Files.getParent(ph) + "/" + OBJ_META_PREFIX + nm;
    }

    public static void set_type(MimeMap mimes, WnObj o, String tp) {
        if (o.isFILE()) {
            if (Strings.isBlank(tp))
                tp = Files.getSuffixName(o.name());

            if (!o.hasType() || !o.isType(tp)) {
                if (Strings.isBlank(tp)) {
                    tp = "txt";
                }
                o.type(tp);
            }

            String mime = mimes.getMime(o.type());
            o.mime(mime);
        }
    }

    public static boolean hasVCLibrary() {
        String vcPath = System.getenv("VIDEO_CONVERT_PATH");
        return !Strings.isBlank(vcPath);
    }

    public static class thumbnail {
        public final static String bgcolor_white = "rgb(255,255,255)";
        public final static String bgcolor_black = "rgb(0,0,0)";
        public final static String bgcolor_default = bgcolor_black;
        public final static String bgcolor_trans = "rgba(255,255,255,0)";
        public final static String size_256 = "256x256";
        public final static String size_128 = "128x128";
        public final static String size_64 = "64x64";
        public final static String size_32 = "32x32";
        public final static String size_24 = "24x24";
        public final static String size_16 = "16x16";
    }

    /**
     * 获得一个copy过来的文件, 推荐用完直接删除
     * 
     * @param obj
     * @return 文件
     */
    public static File getCopyFile(WnIo io, WnObj obj) {
        // 非文件类, size为0就不要掺和了
        if (obj.race() != WnRace.FILE || obj.size() == 0) {
            return null;
        }
        // 生成临时文件
        OutputStream out = null;
        File cf = null;
        try {
            cf = File.createTempFile(Strings.alignLeft(obj.name(), 3, 'a'),
                                     !Strings.isBlank(obj.type()) ? "." + obj.type() : null);
            out = new FileOutputStream(cf);
            io.readAndClose(obj, out);
            out.flush();
            out.close();
            out = null;
        }
        catch (IOException e) {
            throw Err.wrap(e);
        }
        finally {
            if (out != null)
                Streams.safeClose(out);
        }
        return cf;
    }
}
