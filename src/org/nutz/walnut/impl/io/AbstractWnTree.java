package org.nutz.walnut.impl.io;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.impl.io.mnt.LocalFileMounter;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public abstract class AbstractWnTree implements WnTree {

    private static final Pattern regex_id_mnt2 = Pattern.compile("^([\\d\\w]+)://(.+)$");
    private static final Pattern regex_id_mnt3 = Pattern.compile("^([0-9a-v]{4,26}):([\\d\\w]+)://(.+)$");

    protected WnObj root;

    protected MimeMap mimes;

    private Map<String, WnMounter> mounters;

    public AbstractWnTree() {
        mounters = new HashMap<String, WnMounter>();
        mounters.put("file", new LocalFileMounter());
    }

    @Override
    public void _clean_for_unit_test() {}

    @Override
    public WnObj getRoot() {
        return root.clone();
    }

    @Override
    public String getRootId() {
        return root.id();
    }

    @Override
    public boolean isRoot(String id) {
        return root.isSameId(id);
    }

    @Override
    public boolean isRoot(WnObj o) {
        return root.isSameId(o);
    }

    public void setRoot(WnObj root) {
        root.setTree(this);
        this.root = root;
    }

    @Override
    public WnObj get(final String id) {
        if (null == id)
            return null;

        // 如果是 mount 的 ID
        // 那么形式应该类似 34cdqef:file://path/to/file
        Matcher m = regex_id_mnt3.matcher(id);
        if (m.find()) {
            // 得到挂载点对象
            String mntId = m.group(1);
            WnObj mo = this.checkById(mntId);

            // 分析出挂载点类型以及值
            String mntType = m.group(2);
            String val = m.group(3);
            String[] ss = Strings.splitIgnoreBlank(val, "[/]");

            // 返回挂载对象
            return __eval_mnt_obj(mo, mntType, ss, 0, ss.length);
        }

        // 如果是不完整的 ID
        if (!id.matches("[0-9a-v]{26}")) {
            WnQuery q = new WnQuery().limit(2);
            q.setv("id", Pattern.compile("^" + id));
            q.limit(2);
            List<WnObj> objs = this.query(q);
            if (objs.isEmpty())
                return null;
            if (objs.size() > 1)
                throw Er.create("e.io.obj.get.shortid", id);
            return objs.get(0).setTree(this);
        }

        // 如果是完整的 ID
        WnObj o = _get_my_node(id);
        o.remove("ph");
        if (null != o)
            o.setTree(this);

        // 最后校验一下权限
        return Wn.WC().whenAccess(o);
    }

    private WnObj __eval_mnt_obj(WnObj mo,
                                 String mntType,
                                 String[] paths,
                                 int fromIndex,
                                 int toIndex) {
        // 根据类型得到挂载的实现
        WnMounter wm = mounters.get(mntType);
        if (null == wm)
            throw Er.createf("e.io.mnt.unknownType",
                             "%s://%s",
                             mntType,
                             Lang.concat(fromIndex, toIndex - fromIndex, '/', paths));

        return wm.get(mimes, mo, paths, fromIndex, toIndex);
    }

    protected abstract WnObj _get_my_node(String id);

    private void __assert_parent_can_create(WnObj p, WnRace race) {
        // 文件下面不能再有子对象
        if (p.isFILE()) {
            throw Er.create("e.io.tree.nd.file_as_parent", p);
        }

        // 如果是映射节点，只读
        if (p.isMount()) {
            throw Er.create("e.io.tree.c.readonly.mnt", p);
        }
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

        // 尝试从后查找，如果有 id:xxx 那么就截断，因为前面的就木有意义了
        for (int i = toIndex - 1; i >= fromIndex; i--) {
            String nm = paths[i];
            if (nm.startsWith("id:")) {
                p = this.checkById(nm.substring(3));
                fromIndex = i + 1;
                break;
            }
        }

        // 用尽路径元素了，则直接返回
        if (fromIndex >= toIndex)
            return p;

        // 确保读取所有的父
        p.loadParents(null, false);

        // 确保是目录
        if (!p.isDIR()) {
            p = p.parent();
        }

        // 得到节点检查的回调接口
        WnContext wc = Wn.WC();
        WnSecurity secu = wc.getSecurity();

        if (null != secu) {
            p = __enter_dir(p, secu);
        }

        // 处理挂载节点
        if (p.isMount()) {
            Matcher m = regex_id_mnt2.matcher(p.mount());
            if (m.find()) {
                String mntType = m.group(1);
                return this.__eval_mnt_obj(p, mntType, paths, fromIndex, toIndex);
            } else {
                throw Er.create("e.io.mnt.invalid", p.mount());
            }
        }

        // 逐个进入目标节点的父
        WnObj nd;
        String nm;
        int rightIndex = toIndex - 1;
        for (int i = fromIndex; i < rightIndex; i++) {
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
            if (nm.startsWith("^") || nm.contains("*")) {
                WnQuery q = Wn.Q.pid(p).setv("nm", nm).limit(1);
                nd = Lang.first(this.query(q));
            }
            // 找子节点，找不到，就返回 null
            else {
                nd = this._fetch_one_by_name(p, nm);
            }
            if (null == nd)
                return null;

            // 设置节点
            nd.setTree(this);
            nd.setParent(p);
            nd.path(p.path()).appendPath(nd.name());

            // 确保节点可进入
            if (null != secu) {
                nd = __enter_dir(nd, secu);
            }

            // 处理挂载节点
            if (nd.isMount()) {
                Matcher m = regex_id_mnt2.matcher(nd.mount());
                if (m.find()) {
                    String mntType = m.group(1);
                    return this.__eval_mnt_obj(nd, mntType, paths, i + 1, toIndex);
                } else {
                    throw Er.create("e.io.mnt.invalid", nd.mount());
                }
            }

            // 指向下一个节点
            p = nd;
        }

        // 最后再检查一下目标节点
        nm = paths[rightIndex];

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
            nd = Lang.first(this.query(q));
        }
        // 仅仅是普通名称
        else {
            nd = this._fetch_one_by_name(p, nm);
        }

        if (null == nd)
            return null;

        // 设置节点
        nd.setTree(this);
        nd.setParent(p);
        nd.path(p.path()).appendPath(nd.name());

        // 确保节点可以访问
        nd = wc.whenAccess(nd);

        // 搞定了，返回吧
        return nd;
    }

    private WnObj __enter_dir(WnObj o, WnSecurity secu) {
        WnObj dir = secu.enter(o);
        // 肯定遇到了链接目录
        if (!dir.isSameId(o)) {
            dir.name(o.name());
            dir.setParent(o.parent());
        }
        return dir;
    }

    protected abstract WnObj _fetch_one_by_name(WnObj p, String name);

    protected abstract WnObj _do_append(WnObj p, WnObj nd, String newName);

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
    public WnObj create(WnObj p,
                        final String[] paths,
                        final int fromIndex,
                        int toIndex,
                        WnRace race) {
        if (null == p) {
            p = root.clone();
        }

        // 创建所有的父
        final int rightIndex = toIndex - 1;
        final WnObj p0 = p;
        WnObj o = Wn.WC().synctimeOff(new Proton<WnObj>() {
            protected WnObj exec() {
                WnObj p1 = p0;
                WnObj nd = null;
                for (int i = fromIndex; i < rightIndex; i++) {
                    nd = fetch(p1, paths, i, i + 1);
                    // 有节点的话继续下一个路径
                    if (null != nd) {
                        p1 = nd;
                        continue;
                    }
                    // 没有节点，创建一系列目录节点Ï
                    for (; i < rightIndex; i++) {
                        p1 = createById(p1, null, paths[i], WnRace.DIR);
                    }
                }
                return nd == null ? p1 : nd;
            }
        });

        // 创建自身节点
        return createById(o, null, paths[rightIndex], race);
    }

    protected abstract void _create_node(WnObj o);

    @Override
    public WnObj createById(WnObj p, String id, String name, WnRace race) {
        // 得到节点检查的回调接口
        WnContext wc = Wn.WC();
        WnSecurity secu = wc.getSecurity();

        if (null == p)
            p = root.clone();

        // 创建前，检查一下父节点和要创建的节点类型是否匹配
        __assert_parent_can_create(p, race);

        // 应对一下回调
        if (null != secu) {
            p = __enter_dir(p, secu);
            p = secu.write(p);
        }

        // 检查一下重名
        __assert_duplicate_name(p, name);

        // 创建自己
        // 创建子节点
        WnBean o = new WnBean();
        if (Strings.isBlank(id))
            id = Wn.genId();

        // 设置元数据
        o.id(id);
        o.race(race);
        o.setTree(this);
        long now = System.currentTimeMillis();
        o.createTime(now);
        o.lastModified(now);
        o.remove("ph");

        // 展开名字
        o.name(Wn.evalName(name, id));
        Wn.set_type(mimes, o, null);

        // 文件设置类型
        if (o.isFILE())
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
                String g = wc.checkGroup();
                String c = wc.checkMe();
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

        // 执行保存
        _create_node(o);

        // 触发同步时间修改
        Wn.Io.update_ancestor_synctime(this, o, false);

        // 触发钩子 & 返回
        return wc.doHook("create", o);
    }

    private void __assert_duplicate_name(WnObj p, String name) {
        if (exists(p, name))
            throw Er.create("e.io.exists", p);
    }

    @Override
    public WnObj rename(WnObj o, String nm) {
        String ph = o.path();
        ph = Files.renamePath(ph, nm);
        return move(o, ph);
    }

    @Override
    public WnObj move(WnObj src, String destPath) {
        // 不用移动
        String srcPath = src.path();
        if (srcPath.equals(destPath)) {
            return src;
        }

        // 肯定要移动了 ...
        WnContext wc = Wn.WC();

        // 调用钩子
        src.setv("_mv_dest", destPath);
        wc.doHook("move", src);

        // 保存之前的 d0,d1
        String old_d0 = src.d0();
        String old_d1 = src.d1();

        // 得到自身的原始的父
        WnObj oldSrcParent = src.parent();

        // 确保源是可以访问的
        src = wc.whenAccess(src);

        // 确保源的父是可以写入的
        wc.whenWrite(src.parent());

        // 看看目标是否存在
        String newName = null;
        String taPath = destPath;
        WnObj ta = fetch(null, taPath);

        // 如果不存在，看看目标的父是否存在，并且可能也同时要改名
        if (null == ta) {
            taPath = Files.getParent(taPath);
            ta = fetch(null, taPath);
            newName = Files.getName(destPath);
        }
        // 如果存在的是一个文件
        else if (ta.isFILE()) {
            throw Er.create("e.io.exists", destPath);
        }

        // 还不存在不能忍啊
        if (null == ta) {
            throw Er.create("e.io.noexists", taPath);
        }

        // 确认目标能写入
        wc.whenWrite(ta);

        // 改变名称和类型
        if (null != newName) {
            src.name(newName);
            Wn.set_type(mimes, src, null);
        }

        // 改变父
        src.setParent(ta);

        // 更新一下索引的记录
        set(src, "^d0|d1|nm|pid|tp|mime$");

        // 如果是目录，且d0,d1 改变了，需要递归
        if (src.isDIR()) {
            final String d0 = src.d0();
            final String d1 = src.d1();
            if (!Lang.equals(d0, old_d0) || !Lang.equals(d1, old_d1)) {
                this.walk(src, new Callback<WnObj>() {
                    public void invoke(WnObj obj) {
                        obj.d0(d0).d1(d1);
                        set(obj, "^d0|d1$");
                    }
                }, WalkMode.DEPTH_NODE_FIRST);
            }
        }

        // 触发同步时间修改
        Wn.Io.update_ancestor_synctime(this, src, false);

        // 如果对象换了父节点，之前的父节点也要被触发修改时间
        if (!oldSrcParent.isSameId(src.parentId())) {
            Wn.Io.update_ancestor_synctime(this, oldSrcParent, true);
        }

        // 返回
        return src;
    }

    @Override
    public void set(final WnObj o, String regex) {
        _set(o.id(), o.toMap4Update(regex));
    }

    protected abstract void _set(String id, NutMap map);

    protected void _do_walk_children(WnObj p, final Callback<WnObj> callback) {
        this.each(Wn.Q.pid(null == p ? getRootId() : p.id()), new Each<WnObj>() {
            public void invoke(int index, WnObj nd, int length) {
                callback.invoke(nd);
            }
        });
    }

    @Override
    public void walk(WnObj p, final Callback<WnObj> callback, final WalkMode mode) {
        // DEPTH_LEAF_FIRST
        if (WalkMode.DEPTH_LEAF_FIRST == mode) {
            __walk_DEPTH_LEAF_FIRST(p, callback);
        }
        // DEPTH_NODE_FIRST
        else if (WalkMode.DEPTH_NODE_FIRST == mode) {
            __walk_DEPATH_NODE_FIRST(p, callback);
        }
        // 广度优先
        else if (WalkMode.BREADTH_FIRST == mode) {
            __walk_BREADTH_FIRST(p, callback);
        }
        // 仅叶子节点
        else if (WalkMode.LEAF_ONLY == mode) {
            __walk_LEAF_ONLY(p, callback);
        }
        // 不可能
        else {
            throw Lang.impossible();
        }
    }

    private void __walk_LEAF_ONLY(WnObj p, final Callback<WnObj> callback) {
        _do_walk_children(p, new Callback<WnObj>() {
            public void invoke(WnObj nd) {
                if (nd.isFILE())
                    callback.invoke(nd);
                else
                    __walk_LEAF_ONLY(nd, callback);
            }
        });
    }

    private void __walk_BREADTH_FIRST(WnObj p, final Callback<WnObj> callback) {
        final List<WnObj> list = new LinkedList<WnObj>();
        _do_walk_children(p, new Callback<WnObj>() {
            public void invoke(WnObj nd) {
                callback.invoke(nd);
                if (!nd.isFILE())
                    list.add(nd);
            }
        });
        for (WnObj nd : list)
            __walk_BREADTH_FIRST(nd, callback);
    }

    private void __walk_DEPATH_NODE_FIRST(WnObj p, final Callback<WnObj> callback) {
        _do_walk_children(p, new Callback<WnObj>() {
            public void invoke(WnObj nd) {
                callback.invoke(nd);
                if (!nd.isFILE()) {
                    __walk_DEPATH_NODE_FIRST(nd, callback);
                }
            }
        });
    }

    private void __walk_DEPTH_LEAF_FIRST(WnObj p, final Callback<WnObj> callback) {
        _do_walk_children(p, new Callback<WnObj>() {
            public void invoke(WnObj nd) {
                if (!nd.isFILE()) {
                    __walk_DEPTH_LEAF_FIRST(nd, callback);
                }
                callback.invoke(nd);
            }
        });
    }

    @Override
    public void delete(WnObj nd) {
        // 递归删除所有的子孙
        if (nd.isDIR()) {
            this.each(Wn.Q.pid(nd), new Each<WnObj>() {
                public void invoke(int index, WnObj child, int length) {
                    delete(child);
                }
            });
        }

        // 删除自身
        _delete_self(nd);
    }

    protected abstract void _delete_self(WnObj nd);

    @Override
    public boolean exists(WnObj p, String path) {
        return null != fetch(p, path);
    }

    @Override
    public boolean existsId(String id) {
        return null != get(id);
    }

    @Override
    public WnObj checkById(String id) {
        WnObj o = get(id);
        if (null == o)
            throw Er.create("e.io.noexists", "id:" + id);
        return o;
    }

    @Override
    public WnObj check(WnObj p, String path) {
        WnObj o = fetch(p, path);
        if (null == o)
            throw Er.create("e.io.noexists", path);
        return o;
    }

    @Override
    public WnObj createIfNoExists(WnObj p, String path, WnRace race) {
        WnObj o = fetch(p, path);
        if (null == o)
            return create(p, path, race);

        if (!o.isRace(race))
            throw Er.create("e.io.create.invalid.race", path + " ! " + race);

        return o;
    }

    @Override
    public int each(WnQuery q, final Each<WnObj> callback) {
        final boolean autoPath = Wn.WC().isAutoPath();
        final WnTree tree = this;
        final WnContext wc = Wn.WC();
        return _each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj o, int length) {
                o = wc.whenAccess(o);
                if (null == o)
                    return;
                o.setTree(tree);
                // 确保有全路径
                if (autoPath)
                    o.path();
                if (null != callback)
                    callback.invoke(index, o, length);
            }
        });
    }

    protected abstract int _each(WnQuery q, Each<WnObj> callback);

    @Override
    public WnObj getOne(WnQuery q) {
        final WnObj[] re = new WnObj[1];
        if (null != q)
            q.limit(1);
        each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj obj, int length) {
                re[0] = obj;
                Lang.Break();
            }
        });
        return re[0];
    }

    @Override
    public List<WnObj> query(WnQuery q) {
        final List<WnObj> list = new LinkedList<WnObj>();
        each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj obj, int length) {
                list.add(obj);
            }
        });
        return list;
    }

    @Override
    public boolean hasChild(WnObj p) {
        return null != getOne(Wn.Q.pid(p.id()));
    }

}
