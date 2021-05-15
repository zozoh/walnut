package org.nutz.walnut.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.castor.Castors;
import org.nutz.ioc.Ioc;
import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.LoopException;
import org.nutz.lang.Maths;
import org.nutz.lang.Mirror;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.random.R;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.mapl.Mapl;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.box.WnServiceFactory;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.sys.cron.WnSysCronApi;
import org.nutz.walnut.ext.sys.schedule.WnSysScheduleApi;
import org.nutz.walnut.ext.sys.task.WnSysTaskApi;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.web.Webs.Err;

/**
 * Walnut 系统的各种帮助函数集合
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class Wn {

    /**
     * 记录系统运行时信息
     */
    private static WnSysRuntime _rt = null;

    /**
     * @return 系统运行时对象
     */
    public static WnSysRuntime getRuntime() {
        return _rt;
    }

    public static void initRuntime(String nodeName) {
        _rt = new WnSysRuntime(nodeName);
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static boolean matchs(String str, String regex) {
        Pattern p = Regex.getPattern(regex);
        return p.matcher(str).matches();
    }

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

    public static boolean isFullObjId(final String id) {
        if (null == id)
            return false;
        return id.matches("^[0-9a-v]{26}(:.+)?$");
    }

    public static WnObj getObj(WnSystem sys, String str) {
        return getObj(sys.io, sys.session, str);

    }

    public static WnObj getObj(WnIo io, WnAuthSession se, String str) {
        return getObj(io, se.getVars(), str);

    }

    public static WnObj getObj(WnIo io, NutBean vars, String str) {
        // 用 ID! 不可以，否则的话，名字为 ID 的对象就取不到了
        // if (isFullObjId(str)) {
        // return io.get(str);
        // }
        // 用路径
        String path = normalizeFullPath(str, vars);
        return io.fetch(null, path);
    }

    public static WnObj getObj(WnIo io, String str) {
        if (Strings.isBlank(str))
            return null;

        // 用 ID! 不可以，否则的话，名字为 ID 的对象就取不到了
        // if (isFullObjId(str)) {
        // return io.get(str);
        // }
        // 用路径
        return io.fetch(null, str);
    }

    public static WnObj checkObj(WnIo io, String str) {
        WnObj o = getObj(io, str);
        if (null == o)
            throw Er.create("e.io.obj.noexists", str);
        return o;
    }

    public static WnObj checkObj(WnIo io, NutBean vars, String str) {
        WnObj o = getObj(io, vars, str);
        if (null == o)
            throw Er.create("e.io.obj.noexists", str);
        return o;
    }

    public static WnObj checkObj(WnSystem sys, String str) {
        return checkObj(sys.io, sys.session, str);

    }

    public static WnObj checkObj(WnIo io, WnAuthSession se, String str) {
        WnObj o = getObj(io, se, str);
        if (null == o)
            throw Er.create("e.io.obj.noexists", str);
        return o;
    }

    /**
     * 得到当前系统环境变量声明的路径列表
     * 
     * @param sys
     *            系统上下文
     * @param varName
     *            环境变量
     * @param dftPh
     *            默认的值
     * 
     * @return 列表
     */
    public static List<WnObj> getPathObjList(WnSystem sys, String varName, String dftPh) {
        NutMap vars = sys.session.getVars();
        String paths = vars.getString(varName, dftPh);
        String[] phs = Strings.splitIgnoreBlank(paths, ":");

        if (null == phs)
            return new LinkedList<WnObj>();

        List<WnObj> list = new ArrayList<>(phs.length);
        for (String ph : phs) {
            WnObj o = getObj(sys, ph);
            if (null != o)
                list.add(o);
        }

        return list;
    }

    public static WnObj getObjIn(WnSystem sys, String rph, List<WnObj> oList) {
        for (WnObj oP : oList) {
            WnObj o = sys.io.fetch(oP, rph);
            if (null != o)
                return o;
        }
        return null;
    }

    public static String appendPath(String... phs) {
        return Disks.appendPath(phs);
    }

    public static Pattern wildcardToRegex(String wildcard) {
        if (null == wildcard)
            return null;
        // TODO zozoh: 应该考虑一些特殊字符 ...
        String s = wildcard.replace("*", ".*");
        return Regex.getPattern("^" + s + "$");
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

    public static String getObjHomePath(WnObj obj) {
        String d0 = obj.d0();
        if ("root".equals(d0)) {
            return "/root";
        }
        String d1 = obj.d1();
        return Wn.appendPath("/", d0, d1);
    }

    public static String tidyPath(WnIo io, String ph) {
        String[] paths = Strings.splitIgnoreBlank(ph, "/");

        // 从后面查找 id:xxx
        String id = null;
        int i = paths.length - 1;
        for (; i >= 0; i--) {
            String nm = paths[i];
            if (nm.startsWith("id:")) {
                id = nm.substring(3).trim();
                break;
            }
        }

        // 找到了
        if (i >= 0) {
            WnObj o = io.checkById(id);
            i++;
            int len = paths.length - i;
            String rph = Strings.join(i, len, "/", paths);
            String aph = o.path();
            return Wn.appendPath(aph, rph);
        }

        // 木有找到
        return ph;
    }

    public static String normalizePath(String ph, WnSystem sys) {
        return normalizePath(ph, sys.session);
    }

    public static String normalizePath(String ph, WnAuthSession se) {
        return normalizePath(ph, se.getVars());
    }

    public static String normalizePath(String ph, NutBean vars) {
        if (Strings.isBlank(ph))
            return ph;
        // 主目录开头
        if (ph.startsWith("~")) {
            ph = Wn.appendPath(vars.getString("HOME"), ph.substring(1));
        }
        // 当前目录开头
        else if (ph.startsWith("./")) {
            ph = Wn.appendPath(vars.getString("PWD", ""), ph.substring(1));
        }

        // 展开环境变量
        return normalizeStr(ph, vars);
    }

    public static String normalizeFullPath(String ph, WnSystem sys) {
        // 嗯，搞一下变量吧
        ph = normalizeFullPath(ph, sys.session);

        // 如果 ph 里面有 ../id:xxx/... 则用这个来截断
        int pos = ph.lastIndexOf("/id:");
        if (pos > 0) {
            ph = ph.substring(pos + 1);
        }

        // 如果 ph 以 id:xxx 开头，将其搞一下
        if (ph.startsWith("id:")) {
            // 啊啊啊,这是mount挂载的id啊
            if (ph.contains("id:") && ph.contains(":%%")) {
                return sys.io.checkById(ph.substring(3)).path();
            }
            pos = ph.indexOf('/');
            if (pos > 0) {
                String id = ph.substring(3, pos);
                WnObj o = sys.io.checkById(id);
                return Wn.appendPath(o.path(), ph.substring(pos + 1));
            }
            // 就是一个 id:xxx 形式的东东
            String id = ph.substring(3);
            WnObj o = sys.io.checkById(id);
            return o.getRegularPath();
        }

        // 返回
        return ph;
    }

    public static String normalizeFullPath(String ph, WnAuthSession se) {
        return normalizeFullPath(ph, se.getVars());
    }

    public static String normalizeFullPath(String ph, NutBean vars) {
        // 防空
        if (Strings.isBlank(ph))
            return ph;

        // 组合上当前目录
        String pwd = vars.getString("PWD", "");
        if (!ph.startsWith("/") && !ph.startsWith("~")) {
            ph = Wn.appendPath(pwd, ph);
        }

        // 格式化当前路径
        String path = normalizePath(ph, vars);

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
        }
        // 特殊诡异情况
        if (re.endsWith("id:"))
            throw new RuntimeException("emtry paht 'id:' is not allow!!! re=" + re);

        // 如果是目录
        if (ph.endsWith("/") && !re.endsWith("/"))
            return re + "/";

        // 返回
        return re;
    }

    public static String normalizeStr(String str, WnSystem sys) {
        return normalizeStr(str, sys.session);
    }

    public static String normalizeStr(String str, WnAuthSession se) {
        return normalizeStr(str, se.getVars());
    }

    public static String normalizeStr(String str, NutBean env) {
        char[] cs = str.toCharArray();
        StringBuilder var = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cs.length; i++) {
            char c = cs[i];

            // 如果是转义字符，那么仅仅能转移 $
            if (c == '\\') {
                i++;
                // 嗯，要转义 $
                if (i < cs.length && '$' == cs[i]) {
                    sb.append('$');
                }
                // 其他 ..
                else {
                    sb.append('\\');
                    i--;
                }
            }
            // 变量
            else if (c == '$') {
                // 清空，并开始记录变量名
                var.setLength(0);
                while (++i < cs.length) {
                    char c2 = cs[i];
                    // 属于变量名，记录
                    if ((c2 >= 'A' && c2 <= 'Z')
                        || (c2 >= 'a' && c2 <= 'z')
                        || (c2 >= '0' && c2 <= '9')
                        || c2 == '_') {
                        var.append(c2);
                    }
                    // 否则退出
                    else {
                        i--;
                        break;
                    }
                }
                // 空变量名，那么就输出个 $ ，否则进行转义
                if (var.length() > 0) {
                    sb.append(env.getString(var.toString(), ""));
                }
                // 就是一个 $
                else {
                    sb.append('$');
                }
            }
            // 其他，计入
            else {
                sb.append(c);
            }
        }

        // 返回整理后的命令
        return sb.toString();
    }

    private static final Pattern EO1 = Regex.getPattern("^:(:*(=|==|!=|->)(.+))$");
    private static final Pattern EO2 = Regex.getPattern("^(->)(.+)$");
    private static final Pattern EO3 = Regex.getPattern("^(==?|!=)([^?]+)(\\?(.*))?$");
    private static final Pattern EO4 = Regex.getPattern("^(([\\w\\d_.]+)\\?\\?)?(.+)$");

    /**
     * 展开一个对象。可以是字符串，数组，集合，Map 等
     * 
     * @param context
     *            上下文
     * @param obj
     *            要被展开的对象
     * @return 展开后的对象
     */
    @SuppressWarnings("unchecked")
    public static Object explainObj(NutBean context, Object obj) {
        // 防守
        if (null == obj)
            return null;

        Mirror<?> mi = Mirror.me(obj);
        // ....................................
        // String
        if (mi.isStringLike()) {
            String str = obj.toString();
            // Escape
            Matcher m = EO1.matcher(str);
            if (m.find()) {
                return m.group(1);
            }

            String m_type = null, m_val = null;
            Object m_dft = null;
            m = EO2.matcher(str);
            if (m.find()) {
                m_type = m.group(1);
                m_val = Strings.trim(m.group(2));
            }
            // Find key in context
            else {
                m = EO3.matcher(str);
                if (m.find()) {
                    m_type = m.group(1);
                    m_val = Strings.trim(m.group(2));

                    // 搞默认值，聪明点，根据值的样子改变对象类型，不要傻傻的做成字符串
                    String dft = Strings.trim(m.group(4));
                    m_dft = dft;
                    // starts with "=" auto covert to JS value
                    if (dft != null) {
                        if (dft.startsWith("=") || "==".equals(m_type)) {
                            m_dft = Ws.toJavaValue(dft);
                        } else {
                            m_dft = Ws.trim(dft);
                        }
                    }
                }
            }
            // Matched
            if (null != m_type) {
                // ==xxx # Get Boolean value now
                if ("==".equals(m_type)) {
                    Object v = Mapl.cell(context, m_val);
                    if (null == v)
                        return Boolean.FALSE;
                    return Castors.me().castTo(v, Boolean.class);
                }
                // !=xxx # Revert Boolean value now
                if ("==".equals(m_type)) {
                    Object v = Mapl.cell(context, m_val);
                    if (null == v)
                        return Boolean.TRUE;
                    return !Castors.me().castTo(v, Boolean.class);
                }
                // =xxx # Get Value Now
                if ("=".equals(m_type)) {
                    if (".." == m_val) {
                        return context;
                    }
                    return context.getOr(m_val, m_dft);
                }
                // Render template
                if ("->".equals(m_type)) {
                    String test = null;
                    String tmpl = m_val;
                    Matcher m2 = EO4.matcher(m_val);
                    if (m2.find()) {
                        test = m2.group(2);
                        tmpl = m2.group(3);
                    }
                    if (null != test) {
                        Object tv = Mapl.cell(context, test);
                        if (null == tv || !Castors.me().castTo(tv, Boolean.class)) {
                            return null;
                        }
                    }
                    return Tmpl.exec(tmpl, context);
                }
            }
        }
        // ....................................
        // Array
        if (mi.isArray()) {
            int len = Lang.eleSize(obj);
            Object[] arr = new Object[len];
            Lang.each(obj, new Each<Object>() {
                public void invoke(int index, Object ele, int length) {
                    Object v = explainObj(context, ele);
                    arr[index] = v;
                }
            });
            return arr;
        }
        // ....................................
        // 集合
        if (mi.isCollection()) {
            int len = Lang.eleSize(obj);
            List<Object> list = new ArrayList<>(len);
            Lang.each(obj, new Each<Object>() {
                public void invoke(int index, Object ele, int length) {
                    Object v = explainObj(context, ele);
                    list.add(v);
                }
            });
            return list;
        }
        // ....................................
        // Map
        else if (mi.isMap()) {
            NutMap map = NutMap.WRAP((Map<String, Object>) obj);

            // 仅仅是映射
            String valKey = map.getString("key");
            Object mapping = map.get("mapping");
            Object dftValue = map.get("dft");
            if (!Strings.isBlank(valKey) && null != mapping && (mapping instanceof Map)) {
                Object val = context.get(valKey);
                if (null != val) {
                    Map<String, Object> valMapping = (Map<String, Object>) mapping;
                    Object reVal = valMapping.get(val);
                    if (null == reVal) {
                        reVal = dftValue;
                    }
                    return reVal;
                }
                return val;
            }

            // 递归
            NutMap reMap = new NutMap();
            for (Map.Entry<String, Object> en : map.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                Object v2 = explainObj(context, val);
                reMap.put(key, v2);
            }
            return reMap;
        }
        // 其他就直接返回了
        return obj;
    }

    @SuppressWarnings("unchecked")
    public static Object translate(Object input, NutBean mapping) {
        // 防守
        if (null == input)
            return null;

        Mirror<?> mi = Mirror.me(input);
        // ....................................
        // Array
        if (mi.isArray()) {
            int len = Array.getLength(input);
            Object[] arr = new Object[len];
            for (int i = 0; i < len; i++) {
                Object val = Array.get(input, i);
                Object v2 = translate(val, mapping);
                arr[i] = v2;
            }
            return arr;
        }
        // ....................................
        // 集合
        if (mi.isCollection()) {
            Collection<?> col = (Collection<?>) input;
            List<Object> list = new ArrayList<>(col.size());
            for (Object v : col) {
                Object v2 = translate(v, mapping);
                list.add(v2);
            }
            return list;
        }
        // ....................................
        // Map
        else if (mi.isMap()) {
            NutMap inputMap = NutMap.WRAP((Map<String, Object>) input);
            NutMap reMap = new NutMap();
            for (Map.Entry<String, Object> en : mapping.entrySet()) {
                String key = en.getKey();
                String kin = en.getValue().toString();
                Object val = inputMap.get(kin);
                reMap.put(key, val);
            }
            return reMap;
        }
        // 其他就直接返回了
        return input;
    }

    public static Each<WnObj> eachLooping(Each<WnObj> callback) {
        final WnContext wc = Wn.WC();
        return new Each<WnObj>() {
            public void invoke(int index, WnObj obj, int length)
                    throws ExitLoop, ContinueLoop, LoopException {
                WnObj o = wc.whenAccess(obj, true);
                if (null == o)
                    Lang.Continue();
                if (wc.isAutoPath()) {
                    o.path();
                }
                callback.invoke(index, o, length);
            }
        };
    }

    public static WnContext WC() {
        WnContext wc = Wn.Ctx.get();
        if (null == wc) {
            wc = new WnContext();
            Ctx.set(wc);
        }
        return wc;
    }

    public static class Service {

        public static WnServiceFactory services(Ioc ioc) {
            return ioc.get(WnServiceFactory.class, "serviceFactory");
        }

        public static WnAuthService auth(Ioc ioc) {
            return ioc.get(WnAuthService.class, "sysAuthService");
        }

        public static WnSysTaskApi tasks(Ioc ioc) {
            return ioc.get(WnSysTaskApi.class, "safeSysTaskService");
        }

        public static WnSysScheduleApi schedules(Ioc ioc) {
            return ioc.get(WnSysScheduleApi.class, "safeSysScheduleService");
        }

        public static WnSysCronApi crons(Ioc ioc) {
            return ioc.get(WnSysCronApi.class, "sysCronService");
        }

        public static WnBoxService boxes(Ioc ioc) {
            return ioc.get(WnBoxService.class, "boxService");
        }

        public static WnIo io(Ioc ioc) {
            return ioc.get(WnIo.class, "io");
        }
    }

    public static class Mime {
        public static String getGroupName(String mime, String dft) {
            if (!Strings.isBlank(mime)) {
                int pos = mime.indexOf("/");
                if (pos > 0) {
                    return mime.substring(0, pos);
                }
            }
            return dft;
        }
    }

    public static class Cmd {

        public static final int JOIN_CURRENT = 1;

        public static final int NOEXISTS_NULL = 1 << 1;

        public static final int NOEXISTS_IGNORE = 1 << 2;

        public static boolean is(int mode, int mask) {
            return Maths.isMask(mode, mask);
        }

        public static boolean isJoinCurrent(int mode) {
            return is(mode, JOIN_CURRENT);
        }

        public static boolean isNoExistsNull(int mode) {
            return is(mode, NOEXISTS_NULL);
        }

        public static boolean isNoExistsIgnore(int mode) {
            return is(mode, NOEXISTS_IGNORE);
        }

    }

    /**
     * Cookie 中 Walnut 用户的 Session ID 键
     */
    public static final String AT_SEID = "SEID";

    public static class MACRO {
        public static final String UPDATE_ENVS = "update_envs";
        public static final String CHANGE_SESSION = "change_session";
    }

    public static class Io {

        // da39a3ee5e6b4b0d3255bfef95601890afd80709
        public static final String EMPTY_SHA1 = Lang.sha1("");

        public static final int R = 1 << 2;

        public static final int W = 1 << 1;

        public static final int X = 1;

        public static final int RW = R | W;

        public static final int RX = R | X;

        public static final int RWX = R | W | X;

        public static final int WX = W | X;

        public static final int NO_PVG = -999;

        public static boolean isEmptySha1(String sha1) {
            return Strings.isBlank(sha1) || EMPTY_SHA1.equals(sha1);
        }

        public static String octalModeToStr(String octalMode) {
            int mode = Integer.parseInt(octalMode, 8);
            return modeToStr(mode);
        }

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

        public static String modeToOctal(int md) {
            return Integer.toOctalString(md);
        }

        public static String octalModeFromStr(String mds) {
            int md = modeFromStr(mds);
            return Integer.toOctalString(md);
        }

        public static int modeFromOctalMode(String octalMode) {
            return Integer.parseInt(octalMode, 8);
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
         * @param io
         *            IO 接口
         * @param o
         *            对象
         * @param includeSelf
         *            是否也检视自身的同步时间
         */
        public static void update_ancestor_synctime(final WnIo io,
                                                    final WnObj o,
                                                    final boolean includeSelf,
                                                    final long now) {
            WnContext wc = Wn.WC();

            // 防止无穷递归
            if (wc.isSynctimeOff())
                return;

            // 读取所有的祖先节点列表
            final List<WnObj> list = new LinkedList<WnObj>();
            o.loadParents(list, false);
            final long ams = now > 0 ? now : Wn.now();

            // 关闭同步，然后检查同步时间戳设定
            wc.synctimeOff(new Atom() {
                @Override
                public void run() {
                    // 搞一下自己
                    long syncT = o.syncTime();
                    if (includeSelf && syncT > 0 && syncT != ams) {
                        o.syncTime(ams);
                        io.set(o, "^synt$");
                    }
                    // 检查自己的祖先
                    for (WnObj an : list) {
                        syncT = an.syncTime();
                        if (syncT > 0 && syncT != ams) {
                            an.syncTime(ams);
                            io.set(an, "^synt$");
                        }
                    }

                }
            });
        }

        // copy 时标志位： 递归
        public static final int RECUR = 1;

        // copy 时标志位： 同时 copy 元数据
        public static final int PROP = 1 << 1;

        // copy 时标志位： 显示日志
        public static final int VERBOSE = 1 << 2;

        /**
         * @see #copy(WnSystem, int, WnObj, String, WnObjWalkjFilter)
         */
        public static void copy(WnSystem sys, int mode, WnObj oSrc, String ph_dst) {
            copy(sys, mode, oSrc, ph_dst, null);
        }

        /**
         * @param sys
         *            系统上下文
         * @param mode
         *            copy 位串： RECUR 或者 CREATE_PARENTS
         * @param oSrc
         *            源对象
         * @param ph_dst
         *            目标路径
         * @param filter
         *            过滤器。如果声明，所有返回 false 的将被忽略
         * 
         * @see org.nutz.walnut.util.Wn.Io#RECUR
         * @see org.nutz.walnut.util.Wn.Io#PROP
         */
        public static void copy(WnSystem sys,
                                int mode,
                                WnObj oSrc,
                                String ph_dst,
                                WnObjWalkjFilter filter) {
            // 得到配置信息
            boolean isR = Maths.isMask(mode, Wn.Io.RECUR);

            // 检查源，如果是目录，则必须标识 -r
            if (oSrc.isDIR() && !isR) {
                throw Err.create("e.io.copy.src_is_dir", oSrc);
            }

            // 输出相对路径的基础路径，即当前路径
            String ph_base = sys.getCurrentObj().getRegularPath();

            // 得到目标
            WnObj oDst = Wn.getObj(sys, ph_dst);

            // 目标不存在:
            // 应该先检查一下其父是否存在，如果不存在看看是不是 -p
            // 总之要创建一个目标出来
            if (null == oDst) {
                // 必须存在父
                if (!isR) {
                    WnObj oP = Wn.checkObj(sys, Files.getParent(ph_dst));
                    oDst = sys.io.createIfNoExists(oP, Files.getName(ph_dst), oSrc.race());
                }
                // 否则自由创建
                else {
                    oDst = sys.io.createIfNoExists(null, ph_dst, oSrc.race());
                }
                // 执行 Copy 就好了
                __recur_copy_obj(sys, mode, ph_base, oSrc, oDst, filter);
            }
            // 否则，不能是自己 copy 给自己就好
            else {
                // 自己 copy 自己，不能够啊
                if (oDst.isSameId(oSrc)) {
                    throw Er.create("e.io.copy.self", oSrc);
                }
                // 目标是一个文件夹
                if (oDst.isDIR()) {
                    // 在里面创建与源同名的目标
                    WnObj oDst2 = sys.io.createIfNoExists(oDst, oSrc.name(), oSrc.race());
                    // 执行 Copy
                    __recur_copy_obj(sys, mode, ph_base, oSrc, oDst2, filter);
                }
                // 目标是一个文件
                else if (oDst.isFILE()) {
                    // 源必须是一个文件
                    if (!oSrc.isFILE()) {
                        throw Er.create("e.io.copy.file2dir", oSrc.path() + " ->> " + oDst.path());
                    }
                    // 执行 Copy
                    __recur_copy_obj(sys, mode, ph_base, oSrc, oDst, filter);
                }
                // 靠！什么鬼！
                else {
                    throw Lang.impossible();
                }
            }
        }

        private static void __recur_copy_obj(WnSystem sys,
                                             int mode,
                                             String ph_base,
                                             WnObj oSrc,
                                             WnObj oDst,
                                             WnObjWalkjFilter filter) {
            // 得到配置信息
            boolean isP = Maths.isMask(mode, Wn.Io.PROP);
            boolean isV = Maths.isMask(mode, Wn.Io.VERBOSE);

            // 如果是文件夹
            if (oSrc.isDIR() && oDst.isDIR()) {
                Wn.Io.eachChildren(sys.io, oSrc, new Each<WnObj>() {
                    public void invoke(int index, WnObj o, int length) {
                        // 过滤一下
                        if (null != filter) {
                            if (!filter.match(o))
                                return;
                        }
                        // 执行 copy
                        WnObj oTa = sys.io.createIfNoExists(oDst, o.name(), o.race());
                        __recur_copy_obj(sys, mode, ph_base, o, oTa, filter);
                    }
                });
            }
            // 如果是文件
            else if (oSrc.isFILE() && oDst.isFILE()) {
                // 输出日志
                if (isV) {
                    String rph = Disks.getRelativePath(ph_base, oSrc.path());
                    sys.out.println(rph);
                }

                // 内容 copy
                copyFile(sys.io, oSrc, oDst);

                // 元数据 copy
                if (isP) {
                    NutBean meta = oSrc.pickBy("!^(id|pid|race|ph|nm|d[0-9]|ct|lm|data|sha1|len|thumb|videoc_dir)$");
                    // meta.put("mode", oSrc.mode());
                    // meta.put("group", oSrc.group());
                    // meta.put("tp", oSrc.type());
                    // meta.put("mime", oSrc.mime());
                    sys.io.appendMeta(oDst, meta);
                }
            }
            // 靠，不可能
            else {
                throw Lang.impossible();
            }

        }

        /**
         * 两个文件内容对拷的帮助方法。 如果可以，本函数会自动调用快速 copy
         * 
         * @param io
         *            IO 接口
         * @param src
         *            源文件
         * @param dst
         *            目标文件
         */
        public static void copyFile(WnIo io, WnObj src, WnObj dst) {
            // 两个必须是文件
            if (!src.isFILE() || !dst.isFILE()) {
                throw Er.create("e.copy.nofile", src.path() + " ->> " + dst.path());
            }

            // 如果是 Mount 就傻傻的写流
            if (src.isMount() || src.isLink()) {
                io.writeAndClose(dst, io.getInputStream(src, 0));
            }
            // 执行快速 copy
            else {
                io.copyData(src, dst);
                WnContext wc = Wn.WC();
                wc.doHook("write", dst);
            }
        }

        /**
         * @param base
         *            基础对象
         * @param o
         *            对象
         * @return 给定对象相对于基础对象的路径
         */
        public static String getRelativePath(WnObj base, WnObj o) {
            if (null == base || null == o)
                return null;
            String ph_base = base.getRegularPath();
            String ph_o = o.getRegularPath();
            return Disks.getRelativePath(ph_base, ph_o);
        }

        /**
         * 迭代某个对象所有的子对象，考虑了链接目录和映射
         * 
         * @param io
         *            IO 接口
         * @param o
         *            对象
         * @param callback
         *            回调
         */
        public static void eachChildren(WnIo io, WnObj o, final Each<WnObj> callback) {
            // 没有回调，没必要执行
            if (null == callback)
                return;

            // 展开链接对象
            o = Wn.real(o, io, new HashMap<>());

            // 目录才会被迭代
            if (null != o && o.isDIR()) {
                o = Wn.real(o, io);
                // 挂载对象，则使用 getChildren
                if (o.isMount()) {
                    List<WnObj> children = io.getChildren(o, null);
                    int i = 0;
                    int len = children.size();
                    for (WnObj child : children) {
                        callback.invoke(i++, child, len);
                    }
                }
                // 否则直接查询
                else {
                    io.each(Wn.Q.pid(o), callback);
                }
            }
        }

        /**
         * @param io
         *            IO 接口
         * @param o
         *            要删除的文件或者目录
         * @param isR
         *            是否递归
         * 
         * @param 删除前回调
         */
        public static void doDelete(final WnIo io,
                                    WnObj o,
                                    final boolean isR,
                                    Callback<WnObj> callback) {
            // 调用回调
            if (null != callback)
                callback.invoke(o);

            // 递归
            if (!o.isFILE() && isR) {
                io.each(Wn.Q.pid(o.id()), new Each<WnObj>() {
                    public void invoke(int index, WnObj child, int length) {
                        doDelete(io, child, isR, callback);
                    }
                });
            }
            // 删除自己
            io.delete(o);
        }

    }

    /**
     * 移动操作需要的配置信息
     */
    public static class MV {

        /**
         * 自动修改类型
         */
        public static final int TP = 1;

        /**
         * 自动同步树的 syncTime
         */
        public static final int SYNC = 1 << 1;

        public static boolean isTP(int mode) {
            return Maths.isMask(mode, TP);
        }

        public static boolean isSYNC(int mode) {
            return Maths.isMask(mode, SYNC);
        }
    }

    public static class S {

        // A : 1 : 1
        // W : 2 : 10
        // R : 4 : 100
        // M : 8 : 1000
        // RW: 6 : 110
        // WM: 10 : 1010
        // WA: 3 : 11

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
        public static final int WA = W | A;

        public static boolean canRead(int mode) {
            return Maths.isMask(mode, R);
        }

        public static boolean canWite(int mode) {
            return Maths.isMask(mode, W);
        }

        public static boolean canAppend(int mode) {
            return Maths.isMask(mode, A);
        }

        public static boolean canModify(int mode) {
            return Maths.isMask(mode, M);
        }

        public static boolean canWriteOrAppend(int mode) {
            return Maths.isMask(mode, WA);
        }

        public static boolean isRead(int mode) {
            return mode == R;
        }

        public static boolean isWrite(int mode) {
            return mode == W;
        }

        public static boolean isAppend(int mode) {
            return mode == A;
        }

        public static boolean isWriteModify(int mode) {
            return mode == WM;
        }

        public static boolean isWriteAppend(int mode) {
            return mode == WA;
        }

        public static boolean isReadWrite(int mode) {
            return mode == RW;
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
            // return pid(p.myId());
        }

        public static WnQuery pid(String pid) {
            return new WnQuery().setv("pid", pid);
        }

        public static WnQuery map(String str) {
            return new WnQuery().setAll(Lang.map(str));
        }

        public static WnQuery mapf(String fmt, Object... args) {
            return new WnQuery().setAll(Lang.mapf(fmt, args));
        }

    }

    public static final String OBJ_META_PREFIX = ".wn_obj_meta_";

    public static String metaPath(String ph) {
        String nm = Files.getName(ph);
        return Files.getParent(ph) + "/" + OBJ_META_PREFIX + nm;
    }

    public static void set_type(MimeMap mimes, WnObj o, String tp) {
        if (o.isFILE()) {
            // 根据名称自动获取类型
            if (Strings.isBlank(tp)) {
                tp = Files.getSuffixName(o.name());
                // 类型会被强制设置成小写
                if (null != tp) {
                    // 如果仅仅是数字，全当没有
                    if (tp.matches("^\\d+$")) {
                        tp = null;
                    }
                    // 否则强制转小写
                    else {
                        tp = tp.toLowerCase();
                    }
                }
            }
            // 校验一下类型
            if (!o.hasType() || !o.isType(tp)) {
                if (Strings.isBlank(tp)) {
                    tp = "txt";
                }
                o.type(tp);
            }
            // 根据类型自动设置 MIME
            if (!o.hasMime()) {
                String mime = mimes.getMime(o.type());
                o.mime(mime);
            }
            // 如果定义了 mime 那么也改变
            else if (null != tp) {
                String mime = mimes.getMime(tp, null);
                if (null != mime) {
                    o.mime(mime);
                }
            }
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

    /**
     * 展开字符串的宏。包括:
     * 
     * <pre>
     * %date:now
     * %date:now+3d
     * %date:now-12h
     * %date:now+5m
     * %date:now+12s
     * %date:2019-02-13T23:34:12
     * %ms:now
     * %ms:now+3d
     * %ms:now-12h
     * %ms:now+5m
     * %ms:now+12s
     * %ms:2019-02-13T23:34:12
     * </pre>
     * 
     * @param s
     *            输入的字符串
     * @return 展开后的字符串
     */
    public static Object fmt_str_macro(String s) {
        if (Strings.isBlank(s)) {
            return s;
        }
        Object v2;
        // 日期对象
        if (s.startsWith("%date:")) {
            String str = Strings.trim(s.substring("%date:".length()));
            long ms = Wtime.valueOf(str);
            v2 = Times.D(ms);
        }
        // 毫秒数
        else if (s.startsWith("%ms:")) {
            String str = Strings.trim(s.substring("%ms:".length()));
            v2 = Wtime.valueOf(str);
        }
        // 默认采用原值
        else {
            v2 = s;
        }
        return v2;
    }

    public static Date evalDateBy(String str) {
        long ms = Wtime.valueOf(str);
        if (ms > 0) {
            return new Date(ms);
        }
        return Times.D(str);
    }

    public static long evalDateMs(String str) {
        long ms = Wtime.valueOf(str);
        if (ms > 0) {
            return ms;
        }
        return Times.D(str).getTime();
    }

    /**
     * 将一个字符串格式化成可以被 WnQuery 接受的正则查询字符串
     * 
     * <ul>
     * <li>如果以 <code>^</code> 开头，保留原样
     * <li>否则前面增加 <code>.*</code>
     * </ul>
     * 
     * @param str
     *            查询关键字
     * @return 正则表达式字符串
     */
    public static String toQueryRegex(String str) {
        if (Strings.isBlank(str)) {
            return null;
        }
        if (str.startsWith("^"))
            return str;

        return "^.*" + str;
    }

    /**
     * 处理链接文件，如果是链接对象，则返回其链接的目标对象（仅一层）
     * 
     * @param o
     *            原始对象
     * @param io
     *            IO 读写接口
     * 
     * @return 真实文件对象
     */
    public static WnObj real(WnObj o, WnIo io) {
        if (null != o && o.isLink()) {
            // String oldPath = o.path();
            String ln = o.link();
            // 用 ID
            if (ln.startsWith("id:")) {
                String id = ln.substring("id:".length());
                o = io.get(id);
            }
            // 用路径
            else {
                if (ln.startsWith("/")) {
                    o = io.fetch(null, ln);
                } else {
                    WnObj p = o.parent();
                    o = io.fetch(p, ln);
                }
            }
            // 如果节点不存在
            if (null == o)
                throw Er.create("e.io.obj.noexists", ln);
            // 恢复节点的 path
            // o.path(oldPath);
        }
        return o;
    }

    /**
     * 递归处理链接文件，直到找到原始对象。如果有无穷循环，则返回 null
     * 
     * @param o
     *            原始对象
     * @param io
     *            IO 读写接口
     * @param memo
     *            记录增加展开链接的对象，以防止无穷递归
     * 
     * @return 真实文件对象
     */
    public static WnObj real(WnObj o, WnIo io, Map<String, WnObj> memo) {
        // 无需展开链接
        if (null == o || !o.isLink())
            return o;

        // 如果之前解析过，那么一定会发生无限循环，直接返回 null
        if (memo.containsKey(o.id()))
            return null;
        // 记录
        memo.put(o.id(), o);

        // 解析
        WnObj o2 = real(o, io);

        // 返回递归
        return real(o2, io, memo);
    }

    /**
     * @param io
     *            IO 接口
     * @return 系统配置对象
     */
    public static WnSysConf getSysConf(WnIo io) {
        return Wn.WC().nosecurity(io, new Proton<WnSysConf>() {
            @Override
            protected WnSysConf exec() {
                // TODO @peter确认下这个/etc/sysconf是干什么的 默认好像没有这个文件
                // zzh: 嗯，格式了输出了。 WnSysConf 可以让业务逻辑有机会明确的知道自己所在的服务器地址
                WnObj oSysConf = io.fetch(null, "/etc/sysconf");
                if (oSysConf == null) {
                    return new WnSysConf();
                }
                return io.readJson(oSysConf, WnSysConf.class);
            }
        });
    }

    public static boolean checkEtag(WnObj wobj, HttpServletRequest req, HttpServletResponse resp) {
        String etag = getEtag(wobj);
        if (resp != null)
            resp.setHeader("ETag", etag);
        return etag.equals(req.getHeader("If-None-Match"));
    }

    public static String getEtag(WnObj wobj) {
        String sha1 = wobj.sha1();
        long len = wobj.len();
        long lm = wobj.lastModified();
        return getEtag(sha1, len, lm);
    }

    public static String getEtag(String sha1, File f) {
        long len = f.length();
        long lm = f.lastModified();
        return getEtag(sha1, len, lm);
    }

    public static String getEtag(String sha1, long len, long lastModified) {
        if (Strings.isBlank(sha1)) {
            sha1 = "F";
        } else if (sha1.length() > 6) {
            sha1 = sha1.substring(0, 6);
        }
        return String.format("%s-%s-%s", sha1, len, lastModified);
    }

    /**
     * 获取对象的内容指纹，如果对象是链接对象，会一层层的找到真实文件
     * 
     * @param wobj
     *            输入对象（如果是链接对象会被解开）
     * @param io
     *            IO 接口
     * @return 对象的指纹
     */
    public static String getEtag(WnObj wobj, WnIo io) {
        Map<String, WnObj> memo = new HashMap<>();
        WnObj o = Wn.real(wobj, io, memo);
        return getEtag(o);
    }

    public static void checkRootRole(WnSystem sys) {
        // 检查权限: root 组管理员才能操作
        sys.nosecurity(new Atom() {
            public void run() {
                WnAccount me = Wn.WC().getMe();
                if (sys.auth.isAdminOfGroup(me, "root")) {
                    throw Er.create("e.cmd.mgadmin.only_for_root_admin");
                }
            }
        });
    }

    public static String genSaltPassword(String passwd, String salt) {
        return Lang.sha1(passwd + salt);
    }

    // 对 HTML 代码逃逸
    public static String escapeHtml(String str, boolean trim) {
        // var re = _.isString(str) ? str.replace("<", "&lt;") : str;
        // re = this.__escape_ele.text(str).text();
        if (Strings.isBlank(str))
            return str;

        StringBuilder re = new StringBuilder();
        Pattern REG = Regex.getPattern("[<&>]");
        int pos = 0;
        Matcher m = REG.matcher(str);
        while (m.find()) {
            String ms = m.group(0);
            int bg = m.start();
            int ed = m.end();
            // 补充前面的
            if (pos < bg) {
                re.append(str.substring(pos, bg));
            }
            // `<`
            if ("<".equals(ms)) {
                re.append("&lt;");
            }
            // `&`
            else if ("&".equals(ms)) {
                re.append("&amp;");
            }
            // `>`
            else if (">".equals(ms)) {
                re.append("&gt;");
            }

            // 继续执行
            pos = ed;
        }
        // 补足最后一个
        if (pos < str.length()) {
            re.append(str.substring(pos));
        }

        // 返回吧
        return trim ? Strings.trim(re) : re.toString();
    }

    // 对 HTML 代码逃逸的结果，反逃逸
    public static String unescapeHtml(String str, boolean trim) {
        // var re = _.isString(str) ? str.replace("<", "&lt;") : str;
        // re = this.__escape_ele.text(str).text();
        if (Strings.isBlank(str))
            return str;

        StringBuilder re = new StringBuilder();
        Pattern REG = Regex.getPattern("&lt;|&amp;|&gt;");
        int pos = 0;
        Matcher m = REG.matcher(str);
        while (m.find()) {
            String ms = m.group(0);
            int bg = m.start();
            int ed = m.end();
            // 补充前面的
            if (pos < bg) {
                re.append(str.substring(pos, bg));
            }
            // `<`
            if ("&lt;".equals(ms)) {
                re.append("<");
            }
            // `&`
            else if ("&amp;".equals(ms)) {
                re.append("&");
            }
            // `>`
            else if ("&gt;".equals(ms)) {
                re.append(">");
            }

            // 继续执行
            pos = ed;
        }
        // 补足最后一个
        if (pos < str.length()) {
            re.append(str.substring(pos));
        }

        // 返回吧
        return trim ? Strings.trim(re) : re.toString();
    }

    @SuppressWarnings("unchecked")
    public static NutBean anyToMap(WnObj o, Object meta) {
        // 防守一下
        if (null == meta)
            return null;
        // 转成 Map
        NutBean map = null;
        // 字符串
        if (meta instanceof CharSequence) {
            String str = meta.toString();
            // 是一个正则表达式
            if (str.startsWith("!^") || str.startsWith("^")) {
                map = o.pickBy(str);
            }
            // 当作 JSON
            else {
                map = Lang.map(str);
            }
        }
        // 就是 Map
        else if (meta instanceof Map) {
            map = NutMap.WRAP((Map<String, Object>) meta);
        }
        // 其他的统统不支持
        else {
            throw Er.create("e.io.meta.unsupport", meta.getClass().getName());
        }

        return map;
    }
}
