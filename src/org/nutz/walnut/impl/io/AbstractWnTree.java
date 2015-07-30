package org.nutz.walnut.impl.io;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public abstract class AbstractWnTree implements WnTree {

    private WnObj root;

    public void setRoot(WnObj root) {
        this.root = root;
    }

    @Override
    public WnObj get(final String id) {
        if (null == id)
            return null;

        // 如果是不完整的 ID
        if (!id.matches("[0-9a-v]{26}")) {
            WnQuery q = new WnQuery().limit(2);
            q.setv("id", Pattern.compile("^" + id));
            List<WnObj> objs = this.query(q);
            if (objs.isEmpty())
                return null;
            if (objs.size() > 1)
                throw Er.create("e.io.obj.get.shortid", id);
            return objs.get(0).setTree(this);
        }

        // 如果是完整的 ID
        WnObj o = _get_my_node(id);
        if (null != o)
            o.setTree(this);
        return o;
    }

    protected abstract WnObj _get_my_node(String id);

    private void __assert_parent_can_create(WnObj p, WnRace race) {
        // 文件下面不能再有子对象
        if (p.isFILE()) {
            throw Er.create("e.io.tree.nd.file_as_parent", p);
        }

        // 如果映射到本地目录，则只读
        if (p.isMount("file:")) {
            throw Er.create("e.io.tree.c.readonly.local", p);
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

        // 用尽路径元素了，则直接返回
        if (fromIndex >= toIndex)
            return p;

        // 确保读取所有的父
        p.loadParents(null, false);

        // 得到节点检查的回调接口
        WnSecurity secu = Wn.WC().getSecurity();

        if (null != secu) {
            p = secu.enter(p);
        }

        // 逐个进入目标节点的父
        WnObj nd;
        int rightIndex = toIndex - 1;
        for (int i = fromIndex; i < rightIndex; i++) {
            // 因为支持回退上一级，所以有可能 p 为空
            if (null == p) {
                p = root.clone();
            }

            String nm = paths[i];
            if (nm.equals("..")) {
                nd = p.parent();
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
                nd = secu.enter(nd);
            }

            // 指向下一个节点
            p = nd;
        }

        // 最后再检查一下目标节点
        nd = this._fetch_one_by_name(p, paths[rightIndex]);

        if (null == nd)
            return null;

        // 设置节点
        nd.setTree(this);
        nd.setParent(p);
        nd.path(p.path()).appendPath(nd.name());

        // 确保节点可以访问
        nd = Wn.WC().whenAccess(nd);

        // 搞定了，返回吧
        return nd;
    }

    protected abstract WnObj _fetch_one_by_name(WnObj p, String name);

    public WnObj append(WnObj p, WnObj nd, String newName) {
        if (null == p)
            p = root.clone();

        if (null == newName)
            newName = nd.name();

        // 要移动的节点必须不能是顶级节点
        if (!nd.hasParent()) {
            throw Er.create("e.io.tree.appendRoot", nd);
        }

        // 看看有没有必要移动
        if (nd.isMyParent(p)) {
            if (!nd.name().equals(newName)) {
                return this.rename(nd, newName);
            }
            return nd;
        }

        // 得到节点检查的回调接口
        WnContext wc = Wn.WC();
        WnSecurity secu = wc.getSecurity();

        // 分别检查节点
        if (null != secu) {
            p = secu.write(p);
            secu.write(nd.parent());
        }

        // 如果重名，则禁止移动
        if (null != this.fetch(p, newName)) {
            throw Er.create("e.io.tree.exists", newName);
        }

        // 执行移动
        WnObj newNode = _do_append(p, nd, newName);

        // 返回
        newNode.setTree(nd.tree());
        newNode.path(p.path() + "/" + newName);

        return newNode;
    }

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
    public WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race) {
        if (null == p) {
            p = root.clone();
        }

        // 创建所有的父
        WnObj nd;
        int rightIndex = toIndex - 1;
        for (int i = fromIndex; i < rightIndex; i++) {
            nd = fetch(p, paths, i, i + 1);
            // 有节点的话继续下一个路径
            if (null != nd) {
                p = nd;
                continue;
            }
            // 没有节点，创建一系列目录节点Ï
            for (; i < rightIndex; i++) {
                p = createById(p, null, paths[i], WnRace.DIR);
            }
        }

        // 创建自身节点
        return createById(p, null, paths[rightIndex], race);
    }

    protected abstract WnObj _create_node(WnObj p, String id, String name, WnRace race);

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
            p = secu.enter(p);
            p = secu.write(p);
        }

        // 检查一下重名
        __assert_duplicate_name(p, name);

        // 创建自己
        WnObj nd = _create_node(p, id, name, race);
        nd.setTree(this);
        nd.setParent(p);
        nd.path(p.path()).appendPath(nd.name());

        // 调用回调并返回
        return nd;
    }

    private void __assert_duplicate_name(WnObj p, String name) {
        if (exists(p, name))
            throw Er.create("e.io.exists", p);
    }

    @Override
    public WnObj rename(WnObj nd, String newName) {
        // 得到节点检查的回调接口
        WnSecurity secu = Wn.WC().getSecurity();

        // 应对一下回调
        if (null != secu) {
            nd = secu.enter(nd);
            nd = secu.write(nd);
        }

        // 必须有父，才能改名
        if (!nd.hasParent())
            throw Er.create("e.io.noparent", nd);

        // 确保没有重名
        __assert_duplicate_name(nd.parent(), newName);

        // 执行改名
        nd = _do_rename(nd, newName);

        // 因为不确定子类会不会修改路径和名称，最后统一修改节点的 nm 和 path
        nd.name(newName);
        String ph = nd.path();
        if (!Strings.isBlank(ph)) {
            nd.path(Files.renamePath(ph, newName).replace('\\', '/'));
        }

        // 返回
        return nd;
    }
    
    @Override
    public void set(WnObj o, String regex) {
        _set(o.id(), o.toMap4Update(regex));
    }

    protected abstract void _set(String id, NutMap map);

    protected void _do_walk_children(WnObj p, final Callback<WnObj> callback) {
        this.each(Wn.Q.pid(p), new Each<WnObj>() {
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

}
