package org.nutz.walnut.impl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.api.io.WnTreeFactory;

public abstract class AbstractWnTree implements WnTree {

    protected WnNode treeNode;

    private WnTreeFactory factory;

    protected String rootPath;

    public AbstractWnTree(WnTreeFactory factory) {
        this.factory = factory;
    }

    @Override
    public WnNode getTreeNode() {
        return treeNode;
    }

    @Override
    public void setTreeNode(WnNode treeNode) {
        this.treeNode = treeNode;
        if (null != treeNode) {
            if (null == treeNode.tree())
                treeNode.setTree(this);
            rootPath = Strings.sBlank(treeNode.path(), "");
        }
    }

    @Override
    public boolean isRootNode(WnNode nd) {
        return nd.equals(treeNode);
    }

    @Override
    public WnTreeFactory factory() {
        return factory;
    }

    @Override
    public void _clean_for_unit_test() {}

    @Override
    public WnNode getNode(final String id) {
        final WnNode[] nd = new WnNode[1];

        // 先找自己
        nd[0] = get_my_node(id);

        // 没有的话，从自己的子树里寻找
        if (null == nd[0]) {
            eachMountTree(new Each<WnTree>() {
                public void invoke(int index, WnTree tree, int length) {
                    nd[0] = tree.getNode(id);
                    if (null != nd[0])
                        Lang.Break();
                }
            });
        }
        // 返回吧
        return nd[0];
    }

    protected abstract WnNode get_my_node(String id);

    protected WnNode check_parent(WnNode p, WnRace race) {
        if (p == treeNode)
            return p;

        // 默认用 treeNode
        if (null == p) {
            return treeNode;
        }
        // 指定的节点，看看层级关系

        // 文件下面不能再有子对象
        if (p.isFILE()) {
            throw Er.create("e.io.tree.nd.file_as_parent", p);
        }
        // 对象下面只能有对象
        if (p.isOBJ() && race == WnRace.OBJ) {
            throw Er.create("e.io.tree.nd.child_must_obj", p);
        }
        // 必须在树内，否则不能创建
        if (!p.tree().equals(this)) {
            throw Er.create("e.io.tree.nd.OutOfMount", p + " <!> " + treeNode);
        }
        // 嗯，差不多就这些了
        return p;
    }

    protected void _fill_parent_full_path(WnNode p, WnNode nd) {
        WnNode pnd = null == p ? treeNode : p;
        nd.setParent(pnd);
        nd.setTree(this);
        String pph = pnd.path();
        if (!Strings.isBlank(pph)) {
            nd.path(pph + "/" + nd.name());
        } else {
            nd.path("/" + nd.name());
        }
    }

    public void loadParents(WnNode nd, boolean force, List<WnNode> list) {
        // 确保有上下文列表
        if (null == list)
            list = new LinkedList<WnNode>();

        // 给的节点是书的根节点
        if (treeNode.isSameId(nd)) {
            WnNode root = treeNode.duplicate();
            // 如果顶级节点属于别的树，则用别的树的逻辑加载
            // 否则表示到头了
            if (!root.tree().equals(this)) {
                root.tree().loadParents(root, true, list);
            } else {
                root.path("/");
            }
            list.add(root);
            nd.setParent(root);
            return;
        }

        // 否则根据子树的逻辑加载
        WnNode p = nd.parent();
        if (null == p || force) {
            p = _get_my_parent(nd);
        }
        // 没有父，是不可能的
        if (null == p) {
            throw Lang.impossible();
        }

        // 递归调用父的读取
        this.loadParents(p, force, list);
        list.add(p);
        nd.setParent(p);
        nd.path(p.path() + "/" + nd.name());
    }

    protected abstract WnNode _get_my_parent(WnNode nd);

    @Override
    public WnNode fetch(WnNode p, String path, Callback<WnNode> callback) {
        if (path.startsWith("/")) {
            p = null;
        }
        String[] ss = Strings.splitIgnoreBlank(path, "[/]");
        return fetch(p, ss, 0, ss.length, null);
    }

    @Override
    public WnNode fetch(WnNode p,
                        String[] paths,
                        int fromIndex,
                        int toIndex,
                        Callback<WnNode> callback) {
        // 判断绝对路径
        if (null == p) {
            p = treeNode;
        }

        // 用尽路径元素了，则直接返回
        if (fromIndex >= toIndex)
            return p;

        // 逐个查找
        final WnNode[] nd = Lang.array(p);
        for (int i = fromIndex; i < toIndex; i++) {
            // 找子节点，找不到，就返回 null
            if (eachChildren(nd[0], paths[i], new Each<WnNode>() {
                public void invoke(int index, WnNode child, int length) {
                    nd[0] = child;
                    Lang.Break();
                }
            }) <= 0) {
                return null;
            }

            // 应对一下回调
            if (null != callback) {
                callback.invoke(nd[0]);
            }

            // 如果找到了子节点，这个子节点如果 mount 了另外 tree
            // 则用另外一个 tree 的逻辑来查找
            if (nd[0].isMount()) {
                // 用尽路径元素了，则直接返回
                // zzh: 这个是一个优化，提前做点判断，就不用再递归到函数里面再判断了
                if (i + 1 >= toIndex) {
                    return nd[0];
                }
                WnTree mntTree = factory().check(nd[0].path(), nd[0].mount());
                return mntTree.fetch(null, paths, i + 1, toIndex, callback);
            }

        }
        return nd[0];
    }

    public WnNode append(WnNode p, WnNode nd) {
        // 只有同树的节点才能转移
        p.assertTree(nd.tree());

        // 看看有没有必要移动
        if (nd.isMyParent(p)) {
            return nd;
        }

        // 如果重名，则禁止移动
        if (null != this.fetch(p, nd.name(), null)) {
            throw Er.create("e.io.tree.exists", nd.name());
        }

        // 嗯什么都不做吧，因为子类会先调用这个方法，之后再实现对应的逻辑
        return nd;
    }

    @Override
    public WnNode create(WnNode p, String path, WnRace race, Callback<WnNode> callback) {
        // 是否从树的根部创建
        if (path.startsWith("/")) {
            p = treeNode;
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
        return create(p, paths, 0, len, race, callback);

    }

    @Override
    public WnNode create(WnNode p,
                         String[] paths,
                         int fromIndex,
                         int toIndex,
                         WnRace race,
                         Callback<WnNode> callback) {
        if (null == p) {
            p = treeNode;
        }

        // 创建所有的父
        WnNode nd = p;
        int i = fromIndex;
        for (; i < toIndex - 1; i++) {
            WnNode child = fetch(nd, paths, i, i + 1, callback);
            // 有节点的话 ..
            if (null != child) {
                nd = child;
                // 如果节点挂载到了另外一颗树
                if (nd.isMount()) {
                    WnTree mntTree = factory().check(nd.path(), nd.mount());
                    return mntTree.create(null, paths, i + 1, toIndex, race, callback);
                }
                // 继续下一个路径
                continue;
            }
            // 没有节点，创建一个目录节点
            else {
                nd = createNode(nd, null, paths[i], WnRace.DIR);
                nd.setParent(p);
            }
        }

        // 创建自己
        WnNode me = createNode(nd, null, paths[i], race);
        me.setParent(nd);

        // 更新缓存
        this._flush_buffer();

        return me;
    }

    protected void _do_walk_children(WnNode p, final Callback<WnNode> callback) {
        this.eachChildren(p, null, new Each<WnNode>() {
            public void invoke(int index, WnNode nd, int length) {
                callback.invoke(nd);
            }
        });
    }

    @Override
    public void walk(WnNode p, final Callback<WnNode> callback, final WalkMode mode) {

        // DEPTH_LEAF_FIRST
        if (WalkMode.DEPTH_LEAF_FIRST == mode) {
            _do_walk_children(p, new Callback<WnNode>() {
                public void invoke(WnNode nd) {
                    if (!nd.isFILE()) {
                        walk(nd, callback, mode);
                    }
                    callback.invoke(nd);
                }
            });
        }
        // DEPTH_NODE_FIRST
        if (WalkMode.DEPTH_LEAF_FIRST == mode) {
            _do_walk_children(p, new Callback<WnNode>() {
                public void invoke(WnNode nd) {
                    callback.invoke(nd);
                    if (!nd.isFILE()) {
                        walk(nd, callback, mode);
                    }
                }
            });
        }
        // 广度优先
        else if (WalkMode.BREADTH_FIRST == mode) {
            final List<WnNode> list = new LinkedList<WnNode>();
            _do_walk_children(p, new Callback<WnNode>() {
                public void invoke(WnNode nd) {
                    callback.invoke(nd);
                    list.add(nd);
                }
            });
            for (WnNode nd : list)
                walk(nd, callback, mode);
        }
        // 仅叶子节点
        else if (WalkMode.LEAF_ONLY == mode) {
            _do_walk_children(p, new Callback<WnNode>() {
                public void invoke(WnNode nd) {
                    if (nd.isFILE() || nd.isOBJ())
                        callback.invoke(nd);
                    else
                        walk(nd, callback, mode);
                }
            });

        }
        // 不可能
        else {
            throw Lang.impossible();
        }

    }

    @Override
    public void delete(WnNode nd) {
        // 递归删除所有的子孙
        if (!nd.isFILE() && hasChildren(nd)) {
            this.eachChildren(nd, null, new Each<WnNode>() {
                public void invoke(int index, WnNode child, int length) {
                    delete(child);
                }
            });
        }

        // 删除自身
        delete_self(nd);

        // 更新缓存
        _flush_buffer();
    }

    protected abstract void delete_self(WnNode nd);

    @Override
    public boolean equals(Object obj) {
        if (null == obj)
            return false;
        if (obj == this)
            return true;
        if (obj instanceof WnTree) {
            return treeNode.equals(((WnTree) obj).getTreeNode());
        }
        return false;
    }

    protected abstract void _flush_buffer();
}
