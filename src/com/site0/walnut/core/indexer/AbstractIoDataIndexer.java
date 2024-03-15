package com.site0.walnut.core.indexer;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Files;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WalkMode;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.api.io.WnSecurity;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.core.bean.WnObjId;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;
import com.site0.walnut.util.Wobj;

public abstract class AbstractIoDataIndexer extends AbstractIoIndexer {

    private static final Log log = Wlog.getIO();

    protected AbstractIoDataIndexer(WnObj root, MimeMap mimes) {
        super(root, mimes);
    }

    @Override
    public WnObj checkById(String id) {
        WnObj o = this.get(id);
        if (null == o) {
            throw Er.create("e.io.obj.noexists", "id:" + id);
        }
        return o;
    }

    @Override
    public WnObj check(WnObj p, String path) {
        WnObj o = fetch(p, path);
        if (null == o)
            throw Er.create("e.io.obj.noexists", path);
        return o;
    }

    @Override
    public boolean existsId(String id) {
        WnQuery q = Wn.Q.id(id);
        long re = this.count(q);
        return re > 0;
    }

    @Override
    public WnObj fetch(WnObj p, String path) {
        if (path.startsWith("/")) {
            p = null;
        }
        String[] ss = Strings.splitIgnoreBlank(path, "[/]");
        return fetch(p, ss, 0, ss.length);
    }

    @Override
    public WnObj fetch(WnObj p, String[] paths, int fromIndex, int toIndex) {
        // null 表示从根路径开始
        if (null == p) {
            p = root.clone();
        }
        // ................................................
        // 尝试从后查找，如果有 id:xxx 那么就截断，因为前面的就木有意义了
        for (int i = toIndex - 1; i >= fromIndex; i--) {
            String nm = paths[i];
            if (nm.startsWith("id:")) {
                p = this.get(nm.substring(3));
                if (null == p)
                    return null;
                fromIndex = i + 1;
                break;
            }
        }
        // ................................................
        // 用尽路径元素了，则直接返回
        if (fromIndex >= toIndex)
            return p;
        // ................................................
        // 确保读取所有的父
        p.loadParents(null, false);
        // ................................................
        // 得到节点检查的回调接口
        WnContext wc = Wn.WC();
        WnSecurity secu = wc.getSecurity();

        if (null != secu) {
            p = _enter_dir(p, secu);
        }

        // 确保是目录
        if (!p.isDIR()) {
            p = p.parent();
        }
        // ................................................
        // 逐个进入目标节点的父
        WnObj nd;
        String nm;
        int lastIndex = toIndex - 1;
        for (int i = fromIndex; i < lastIndex; i++) {
            // 因为支持回退上一级，所以有可能 p 为空
            if (null == p) {
                p = root.clone();
            }

            nm = paths[i];

            // 就是当前
            if (".".equals(nm)) {
                continue;
            }

            // 回退一级
            if ("..".equals(nm)) {
                nd = p.parent();
                p = nd;
                continue;
            }
            // 子节点采用的通配符或者正则表达式
            // - 通配符 "*" 会在 WnQuery 转成真正查询条件时，正则表达式化
            if (nm.startsWith("^") || nm.contains("*")) {
                WnQuery q = Wn.Q.pid(p).setv("nm", nm).limit(1);
                nd = Wlang.first(this.query(q));
            }
            // 找子节点，找不到，就返回 null
            else {
                nd = this.fetchByName(p, nm);
            }

            // 找不到了，就返回
            if (null == nd)
                return null;

            // 设置节点
            nd.setParent(p);
            nd.path(p.path()).appendPath(nd.name());

            // 确保节点可进入
            if (null != secu) {
                nd = _enter_dir(nd, secu);
            }

            // 指向下一个节点
            p = nd;
        }
        // ................................................
        // 最后再检查一下目标节点
        nm = paths[lastIndex];

        // 就是返回自己
        if (nm.equals(".")) {
            return p;
        }

        // 纯粹返回上一级
        if (nm.equals("..")) {
            return p.parent();
        }

        // 因为支持回退上一级，所以有可能 p 为空
        if (null == p) {
            p = root.clone();
        }

        // 目标是通配符或正则表达式
        if (nm.startsWith("^") || nm.contains("*")) {
            WnQuery q = Wn.Q.pid(p).setv("nm", nm).limit(1);
            nd = Wlang.first(this.query(q));
        }
        // 仅仅是普通名称
        else {
            nd = this.fetchByName(p, nm);
        }
        // ................................................
        // 最后，可惜，还是为空
        if (null == nd)
            return null;
        // ................................................
        // 设置节点
        nd.setParent(p);
        nd.path(p.path()).appendPath(nd.name());
        // ................................................
        // 确保节点可以访问
        nd = wc.whenAccess(nd, true);

        // ................................................
        // 搞定了，返回吧
        return nd;
    }

    @Override
    public WnObj fetchByName(WnObj p, String name) {
        WnObj o = _fetch_by_name(p, name);
        _complete_obj_by_parent(p, o);
        return o;
    }

    /**
     * 补全对象的一些关键字段
     * 
     * @param p
     *            父对象，如果为 null则用索引管理器根节点
     * @param o
     *            要被补全的对象
     */
    protected void _complete_obj_by_parent(WnObj p, WnObj o) {
        if (null != o) {
            if (null == p) {
                p = this.root;
            }
            // 确保设置索引管理器
            if (o instanceof WnIoObj) {
                ((WnIoObj) o).setIndexer(this);
            }
            // 确保补全两段式 ID
            if (p.isMount() && !o.hasMountRootId()) {
                String rootId = Strings.sBlank(p.mountRootId(), p.id());
                o.mountRootId(rootId);
            }
            // 补全:对象树
            o.putDefault("nm", o.id());
            o.putDefault("pid", p.id());
            o.putDefault("race", WnRace.FILE);
            // 补全:权限
            o.putDefault("c", p.creator());
            o.putDefault("m", p.mender());
            o.putDefault("g", p.group());
            o.putDefault("md", p.mode());
            // 补全映射
            if (p.isMount() && !o.isMount()) {
                o.mount(p.mount());
            }
            // 补全:时间戳
            o.putDefault("ct", p.createTime());
            o.putDefault("lm", o.createTime());
        }
    }

    protected abstract WnObj _fetch_by_name(WnObj p, String name);

    @Override
    public WnObj create(WnObj p, String path, WnRace race) {
        // 是否从树的根部创建
        if (path.startsWith("/")) {
            p = root.clone();
        }

        // 分析路径
        String[] ss = Strings.splitIgnoreBlank(path, "[/]");
        String[] paths = new String[ss.length];
        int len = 0;
        for (String s : ss) {
            // 回退
            if ("..".equals(s)) {
                len = Math.max(len - 1, 0);
            }
            // 当前
            else if (".".equals(s)) {
                continue;
            }
            // 增加
            else {
                paths[len++] = s;
            }
        }

        // 创建
        return create(p, paths, 0, len, race);
    }

    @Override
    public WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race) {
        // 默认从自己的根开始
        if (null == p) {
            p = root.clone();
        }

        // 准备创建
        final int rightIndex = toIndex - 1;
        final WnObj p0 = p;
        final WnContext wc = Wn.WC();

        // 创建所有的父
        WnObj p1 = p0;
        for (int i = fromIndex; i < rightIndex; i++) {
            WnObj nd = fetch(p1, paths, i, i + 1);
            // 确保节点可以进入
            nd = wc.whenEnter(nd, false);

            // 有节点的话继续下一个路径
            if (null != nd) {
                p1 = nd;
                continue;
            }
            // 没有节点，创建目录节点Ï
            for (; i < rightIndex; i++) {
                p1 = createById(p1, null, paths[i], WnRace.DIR);
            }
        }

        // 创建自身节点
        return createById(p1, null, paths[rightIndex], race);
    }

    @Override
    public WnObj createById(WnObj p, String id, String name, WnRace race) {
        // 主目录
        if (null == p)
            p = root.clone();

        // 文件下面不能再有子对象
        if (p.isFILE()) {
            throw Er.create("e.io.create.ParentShouldBeDir", p);
        }

        // 得到节点检查的回调接口
        WnContext wc = Wn.WC();
        WnSecurity secu = wc.getSecurity();

        // 必须需要 race
        if (null == race) {
            throw Er.create("e.io.create.nilRace");
        }

        // 确保有 ID
        if (Strings.isBlank(id)) {
            id = Wn.genId();
        }

        // 展开名称
        name = Wobj.evalName(name, id);

        // 检查名称
        Wobj.assertValidName(name);

        // 应对一下回调
        if (null != secu) {
            p = _enter_dir(p, secu);
            p = secu.write(p, false);
        }

        // 检查重名
        if (null != this.fetchByName(p, name))
            throw Er.createf("e.io.obj.exists", "%s/%s", p.path(), name);

        // 如果对象是映射，则采用两段式ID
        if (p.isMount()) {
            String rootId = p.mountRootId();
            if (Strings.isBlank(rootId)) {
                rootId = p.id();
            }
            id = rootId + ":" + id;
        }

        // 创建自身
        WnIoObj o = new WnIoObj();
        long now = Wn.now();
        o.setIndexer(this);
        o.id(id);
        o.name(name);
        o.race(race);
        o.createTime(now);
        o.lastModified(now);

        // 自动设置类型
        Wn.set_type(mimes, o, null);

        // 关联父节点
        o.setParent(p);

        // 顶级节点，均属于 root
        if (Strings.isBlank(o.d1())) {
            o.creator("root").mender("root").group("root");
        }
        // 设置创建者，以及权限相关
        else {
            try {
                String g = wc.checkMyGroup();
                String c = wc.checkMyName();
                o.creator(c).mender(c).group(g);
            }
            // 线程没设置，用父对象的
            catch (Exception e) {
                o.creator(p.creator()).mender(p.mender()).group(p.group());
            }
        }

        // 计算 d0,d1
        String[] ss = o.dN();

        // 主节点和 home 必须是可以进入的
        if (ss.length == 0 || (ss.length == 1 && ss[0].equals("home"))) {
            o.mode(0755);
        }
        // 二级节点参照父
        else if (ss.length == 2) {
            o.mode(p.mode());
        }
        // 其他的节点统统保护 >o<
        else {
            o.mode(0750);
        }

        // 真正执行创建
        WnObj o2 = _create(o);
        this._complete_obj_by_parent(p, o2);
        return o2;
    }

    @Override
    public WnObj create(WnObj p, WnObj o) {
        if (null == p) {
            throw Er.create("e.io.NilParent");
        }
        // 设置索引管理器
        ((WnIoObj) o).setIndexer(this);
        // 不匹配的父节点
        if (!o.isMyParent(p)) {
            throw Er.create("e.io.NotMyParent", p.id() + " <-> " + o.parentId());
        }

        // 文件下面不能再有子对象
        if (p.isFILE()) {
            throw Er.create("e.io.create.ParentShouldBeDir", p);
        }

        // 得到节点检查的回调接口
        WnContext wc = Wn.WC();
        WnSecurity secu = wc.getSecurity();

        // 首先，对象必须具备一个 race
        o.putDefault("race", WnRace.FILE);

        // 确保有 ID
        if (!o.hasID()) {
            String id = Wn.genId();
            // 如果对象是映射，则采用两段式ID
            if (p.isMount()) {
                String rootId = p.mountRootId();
                if (Strings.isBlank(rootId)) {
                    rootId = p.id();
                }
                id = rootId + ":" + id;
            }
            o.id(id);
        }

        // 展开名称
        String name = Wobj.evalName(o.name(), o.myId());

        // 检查名称
        Wobj.assertValidName(name);
        o.name(name);

        // 应对一下回调
        if (null != secu) {
            p = _enter_dir(p, secu);
            p = secu.write(p, false);
        }

        // 检查重名
        if (null != this.fetchByName(p, name))
            throw Er.createf("e.io.obj.exists", "%s/%s", p.path(), name);

        // 设置默认的创建时间
        if (o.createTime() <= 0) {
            long now = Wn.now();
            o.createTime(now);
        }
        if (o.lastModified() <= 0) {
            o.lastModified(o.createTime());
        }

        // 自动设置类型
        Wn.set_type(mimes, o, o.type());

        // 关联父节点
        o.setParent(p);

        // 顶级节点，均属于 root
        if (Strings.isBlank(o.d1())) {
            o.creator("root").mender("root").group("root");
        }
        // 设置创建者，以及权限相关
        else {
            try {
                String g = wc.checkMyGroup();
                String c = wc.checkMyName();
                o.creator(c).mender(c).group(g);
            }
            // 线程没设置，用父对象的
            catch (Exception e) {
                o.creator(p.creator()).mender(p.mender()).group(p.group());
            }
        }

        // 计算 d0,d1
        String[] ss = o.dN();

        // 主节点和 home 必须是可以进入的
        if (ss.length == 0 || (ss.length == 1 && ss[0].equals("home"))) {
            o.mode(0755);
        }
        // 二级节点参照父
        else if (ss.length == 2) {
            o.mode(p.mode());
        }
        // 其他的节点统统保护 >o<
        else {
            o.mode(0750);
        }

        // 真正执行创建
        WnObj o2 = _create((WnIoObj) o);
        this._complete_obj_by_parent(p, o2);
        return o2;
    }

    protected WnObj _enter_dir(WnObj o, WnSecurity secu) {
        // 检查是否可以进入，同时如果是链接目录，将会被解开
        WnObj dir = secu.enter(o, false);
        // 肯定遇到了链接目录
        if (!dir.isSameId(o)) {
            dir.name(o.name());
            dir.setParent(o.parent());
        }
        return dir;
    }

    protected void _set_quiet(WnObj o, String regex) {
        NutMap map = o.toMap4Update(regex);
        String id = o.myId();
        _set(id, map);
    }

    @Override
    public List<WnObj> query(WnQuery q) {
        final List<WnObj> list = new LinkedList<WnObj>();
        Each<WnObj> looper = Wn.eachLooping(new Each<WnObj>() {
            public void invoke(int index, WnObj obj, int length) {
                list.add(obj);
            }
        });
        this.each(q, looper);
        return list;
    }

    @Override
    public List<WnObj> getChildren(WnObj o, String name) {
        final List<WnObj> list = new LinkedList<WnObj>();
        Each<WnObj> looper = Wn.eachLooping(new Each<WnObj>() {
            public void invoke(int index, WnObj obj, int length) {
                list.add(obj);
            }
        });
        this.eachChild(o, name, looper);
        return list;
    }

    protected abstract WnObj _create(WnIoObj o);

    // protected abstract WnObj _get_by_id(String id);

    protected abstract void _set(String id, NutBean map);

    protected abstract WnIoObj _set_by(WnQuery q, NutBean map, boolean returnNew);

    @Override
    public WnObj rename(WnObj o, String nm) {
        return rename(o, nm, false);
    }

    @Override
    public WnObj rename(WnObj o, String nm, boolean keepType) {
        int mode = Wn.MV.SYNC;
        if (!keepType)
            mode |= Wn.MV.TP;
        return rename(o, nm, mode);
    }

    @Override
    public WnObj rename(WnObj o, String nm, int mode) {
        Wobj.assertValidName(nm);
        String ph = o.path();
        String ph2 = Files.renamePath(ph, nm);
        // 防守
        if (ph.equals(ph2)) {
            return o;
        }
        // 检查重名
        if (this.exists(null, ph2)) {
            throw Er.create("e.io.obj.exists", ph2);
        }
        // 执行移动
        return move(o, ph2, mode);
    }

    @Override
    public WnObj move(WnObj src, String destPath) {
        return move(src, destPath, Wn.MV.TP | Wn.MV.SYNC);
    }

    @Override
    public WnObj move(WnObj src, String destPath, int mode) {
        // 目标是空的，啥也不做
        if (Strings.isBlank(destPath))
            return src;

        // 不用移动
        String srcPath = this.checkById(src.id()).path();
        if (srcPath.equals(destPath)) {
            return src;
        }

        // 将自己移动到自己的子里面，这个不能够啊
        if (destPath.startsWith(srcPath) && srcPath.lastIndexOf('/') < destPath.lastIndexOf('/')) {
            throw Er.create("e.io.mv.parentToChild");
        }

        // 肯定要移动了 ...
        WnContext wc = Wn.WC();

        // 保存之前的 d0,d1
        String old_d0 = src.d0();
        String old_d1 = src.d1();

        // 确保源是可移除的
        src = wc.whenRemove(src, false);

        // 看看目标是否存在
        String newName = null;
        String taPath = destPath;
        String taPaPath = Files.getParent(taPath);
        WnObj ta = null;
        // 看看是不是移动到根呢？
        if (null != taPaPath && !"/".equals(taPaPath) && taPaPath.equals(root.path())) {
            ta = root;
            newName = Files.getName(taPath);
        }

        // 不是移动到根，那么是哪里呢？ 尝试查询一下
        if (null == ta) {
            ta = fetch(null, taPath);
        }

        // 准备最后更新的正则表达式
        String regex = "d0|d1|pid";

        // 如果不存在，看看目标的父是否存在，并且可能也同时要改名
        if (null == ta) {
            ta = fetch(null, taPaPath);
            newName = Files.getName(destPath);
        }
        // 如果存在的是一个文件
        else if (ta.isFILE()) {
            throw Er.create("e.io.obj.exists", destPath);
        }

        // 还不存在不能忍啊
        if (null == ta) {
            throw Er.create("e.io.obj.noexists", taPath);
        }

        // 确认目标能写入
        ta = wc.whenWrite(ta, false);

        // 改变名称和类型
        if (null != newName) {
            src.name(newName);
            regex += "|nm";

            // 还要同时更新类型，好吧
            if (Wn.MV.isTP(mode)) {
                Wn.set_type(mimes, src, null);
                regex += "|tp|mime";
            }
        }

        // 改变父
        src.setParent(ta);

        // 更新一下索引的记录
        _set_quiet(src, "^(" + regex + ")$");

        // 如果是目录，且d0,d1 改变了，需要递归
        if (src.isDIR()) {
            final String d0 = src.d0();
            final String d1 = src.d1();
            if (!Wlang.isEqual(d0, old_d0) || !Wlang.isEqual(d1, old_d1)) {
                this.walk(src, new Callback<WnObj>() {
                    public void invoke(WnObj obj) {
                        obj.d0(d0).d1(d1);
                        _set_quiet(obj, "^d0|d1$");
                    }
                }, WalkMode.DEPTH_NODE_FIRST, null);
            }
        }

        // 返回
        return src;
    }

    @Override
    public void set(WnObj o, String regex) {
        NutMap map = o.toMap4Update(regex);

        // 木有必要更新
        if (map.isEmpty()) {
            return;
        }

        // 改名
        String nm = map.getString("nm");
        map.remove("nm");

        // 改动路径
        String pid = map.getString("pid");
        map.remove("pid");

        // 移动到另外的目录
        if (!Strings.isBlank(pid)) {
            String newPath = this.checkById(pid).path();
            if (!Strings.isBlank(nm)) {
                newPath = Wn.appendPath(newPath, nm);
            } else {
                newPath = Wn.appendPath(newPath, o.name());
            }
            this.move(o, newPath);
        }
        // 仅仅是改名
        else if (!Strings.isBlank(nm)) {
            this.rename(o, nm);
        }

        // 确保对象有写权限
        o = Wn.WC().whenMeta(o, false);

        // 修改元数据
        _set(o.myId(), map);
    }

    @Override
    public WnObj setBy(String id, NutBean map, boolean returnNew) {
        WnObj o = this.checkById(id);
        // 如果改名，先改一下
        if (map.has("nm")) {
            String nm = map.getString("nm");
            map.remove("nm");
            if (!o.isSameName(nm)) {
                o = this.rename(o, nm);
            }
            if (map.isEmpty()) {
                return o;
            }
        }
        // 然后改其他的
        if (!map.isEmpty()) {
            // 恢复暗戳戳的 nm 修改
            if (map.has("nm!")) {
                Object nm = map.remove("nm!");
                map.put("nm", nm);
            }
            // 确保对象有写权限
            o = Wn.WC().whenMeta(o, false);

            // 修改元数据
            this._set(id, map);
            if (returnNew) {
                o = this.get(id);
            }
        }

        // 搞定
        return o;
    }

    @Override
    public WnObj setBy(WnQuery q, NutBean map, boolean returnNew) {
        // 空条件
        if (null == q || q.isEmptyMatch()) {
            return null;
        }

        // 不支持改名和移动目录
        if (null != map) {
            map.remove("nm");
            map.remove("nm!");
            map.remove("pid");
        }
        // 确保对象存在，并有写权限
        WnObj o = this.getOne(q);
        if (null == o)
            return null;

        // 执行修改
        if (map.size() > 0) {
            // 确保对象有写权限
            o = Wn.WC().whenMeta(o, false);

            // 这里再次确保只匹配一个
            q.limit(1);

            // 执行设置
            WnObj o1 = _set_by(q, map, returnNew);

            if (null != o1) {
                o1.remove("ph");
            }
            o = o1;
        }

        // 返回修改前内容
        return o;
    }

    @Override
    public int inc(String id, String key, int val, boolean returnNew) {
        return this.inc(Wn.Q.id(id), key, val, returnNew);
    }

    @Override
    public int eachChild(WnObj o, String name, Each<WnObj> callback) {
        if (null == o) {
            o = this.getRoot();
        }
        WnQuery q = Wn.Q.pid(o.myId());
        if (null != name) {
            // 正则
            if (name.startsWith("^")) {
                q.setv("nm", name);
            }
            // 通配符
            else if (name.contains("*")) {
                String regex = "^" + name.replace("*", ".*");
                q.setv("nm", regex);
            }
            // 精确等于
            else {
                q.setv("nm", name);
            }
        }
        q.asc("nm");
        return this._each(q, o, callback);
    }

    @Override
    public int each(WnQuery q, Each<WnObj> callback) {
        return _each(q, root, new Each<WnObj>() {
            public void invoke(int index, WnObj o, int length) {
                callback.invoke(index, o, length);
            }
        });
    }

    protected abstract int _each(WnQuery q, WnObj pHint, Each<WnObj> callback);

    @Override
    public long countChildren(WnObj o) {
        WnQuery q = Wn.Q.pid(o);
        return count(q);
    }

    @Override
    public boolean hasChild(WnObj p) {
        return countChildren(p) > 0;
    }

    @Override
    public WnObj get(String id) {
        // 防守空 ID
        if (Strings.isBlank(id)) {
            return null;
        }
        // 两段式 ID
        WnObjId oid = new WnObjId(id);

        // 用子类获取
        WnIoObj o = _get_by_id(oid.getMyId());
        if (null == o) {
            return null;
        }

        // 填充自己
        o.mountRootId(oid.getHomeId());
        this._complete_obj_by_parent(root, o);

        // 这里处理一下自己引用自己的对象问题，直接返回吧，这个对象一定是错误的
        if (o.isSameId(o.parentId())) {
            if (log.isWarnEnabled()) {
                log.warnf("!!! pid->self", o.id());
            }
            return o;
        }

        // 搞定
        return o;
    }

    protected abstract WnIoObj _get_by_id(String id);

    @Override
    public WnObj getOne(WnQuery q) {
        final WnObj[] re = new WnObj[1];
        if (q == null)
            q = new WnQuery();
        q.limit(1);

        each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj obj, int length) {
                re[0] = obj;
            }
        });
        return re[0];
    }

    @Override
    public int getInt(String id, String key, int dft) {
        return this.getAs(id, key, Integer.class, dft);
    }

    @Override
    public long getLong(String id, String key, long dft) {
        return this.getAs(id, key, Long.class, dft);
    }

    @Override
    public String getString(String id, String key, String dft) {
        return this.getAs(id, key, String.class, dft);
    }

}
