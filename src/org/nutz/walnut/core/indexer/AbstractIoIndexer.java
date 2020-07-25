package org.nutz.walnut.core.indexer;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.core.WnIoIndexer;
import org.nutz.walnut.core.WnIoMappingFactory;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public abstract class AbstractIoIndexer implements WnIoIndexer {

    protected WnObj root;

    // protected WnIoMappingFactory mappings;

    protected MimeMap mimes;

    protected AbstractIoIndexer(WnObj root, WnIoMappingFactory mappings, MimeMap mimes) {
        this.root = root;
        // this.mappings = mappings;
        this.mimes = mimes;
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
                nd = Lang.first(this.query(q));
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
            nd = Lang.first(this.query(q));
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
        this.eachChild(o, looper);
        return list;
    }

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
        WnObj nd = null;
        for (int i = fromIndex; i < rightIndex; i++) {
            nd = fetch(p1, paths, i, i + 1);
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
        return createById(nd, null, paths[rightIndex], race);
    }

    @Override
    public WnObj createById(WnObj p, String id, String name, WnRace race) {
        // 主目录
        if (null == p)
            p = root.clone();

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
        name = Wn.evalName(name, id);

        // 检查名称
        Wn.assertValidName(name, p.path());

        // 文件下面不能再有子对象
        if (p.isFILE()) {
            throw Er.create("e.io.create.ParentShouldBeDir", p);
        }

        // 应对一下回调
        if (null != secu) {
            p = _enter_dir(p, secu);
            p = secu.write(p, false);
        }

        // 检查重名
        if (null != this.fetchByName(p, name))
            throw Er.createf("e.io.obj.exists", "%s/%s", p.path(), name);

        // 创建自身
        long now = System.currentTimeMillis();
        WnIoObj o = new WnIoObj();
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

        // 触发钩子 & 返回
        return wc.doHook("create", o2);
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
        String id = o.id();
        _set(id, map);
    }

    protected abstract WnObj _create(WnObj o);

    // protected abstract WnObj _get_by_id(String id);

    protected abstract void _set(String id, NutMap map);

    protected abstract WnIoObj _set_by(WnQuery q, NutMap map, boolean returnNew);

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
        Wn.assertValidName(nm, o.path());
        String ph = o.path();
        ph = Files.renamePath(ph, nm);
        return move(o, ph, mode);
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

        // 调用钩子
        wc.doHook("move", src);

        // 确保源是可移除的
        src = wc.whenRemove(src, false);

        // 看看目标是否存在
        String newName = null;
        String taPath = destPath;
        WnObj ta = fetch(null, taPath);

        // 准备最后更新的正则表达式
        String regex = "d0|d1|pid";

        // 如果不存在，看看目标的父是否存在，并且可能也同时要改名
        if (null == ta) {
            taPath = Files.getParent(taPath);
            ta = fetch(null, taPath);
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
        _set(o.id(), map);
    }

    @Override
    public WnObj setBy(String id, NutMap map, boolean returnNew) {
        return setBy(Wn.Q.id(id), map, returnNew);
    }

    @Override
    public WnObj setBy(WnQuery q, NutMap map, boolean returnNew) {
        // 空条件
        if (null == q || q.isEmptyMatch()) {
            return null;
        }

        // 不支持改名和移动目录
        if (null != map) {
            map.remove("nm");
            map.remove("pid");
        }
        // 确保对象存在，并有写权限
        WnObj o = this.getOne(q);
        if (null == o)
            return null;

        o = Wn.WC().whenMeta(o, false);

        // 执行修改
        if (map.size() > 0) {
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
    public WnObj getRoot() {
        return this.root;
    }

    @Override
    public String getRootId() {
        return this.root.id();
    }

    @Override
    public boolean isRoot(String id) {
        return this.root.isSameId(id);
    }

    @Override
    public boolean isRoot(WnObj o) {
        return root.isSameId(o);
    }

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
    public MimeMap mimes() {
        return this.mimes;
    }
}
