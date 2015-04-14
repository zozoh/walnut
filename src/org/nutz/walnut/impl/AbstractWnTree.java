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

    public AbstractWnTree(WnTreeFactory factory, WnNode treeNode) {
        this.treeNode = treeNode;
        this.factory = factory;
        if (null == treeNode.tree())
            treeNode.setTree(this);
    }

    @Override
    public WnNode getTreeNode() {
        return treeNode;
    }

    @Override
    public boolean isRootNode(WnNode nd) {
        return nd.equals(treeNode);
    }

    @Override
    public WnTreeFactory factory() {
        return factory;
    }

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
        }
    }

    @Override
    public WnNode fetch(WnNode p, String path) {
        if (path.startsWith("/")) {
            p = treeNode;
        }
        String[] ss = Strings.splitIgnoreBlank(path, "[/]");
        return fetch(p, ss, 0, ss.length);
    }

    @Override
    public WnNode fetch(WnNode p, String[] paths, int fromIndex, int toIndex) {
        // 判断绝对路径
        if (null == p) {
            p = treeNode;
        }

        // 逐个查找
        final WnNode[] nd = Lang.array(p);
        for (int i = fromIndex; i < toIndex; i++) {
            // 找不到子节点，就返回 null
            if (eachChildren(nd[0], paths[i], new Each<WnNode>() {
                public void invoke(int index, WnNode child, int length) {
                    nd[0] = child;
                    Lang.Break();
                }
            }) <= 0) {
                return null;
            }
            // 如果找到了子节点，这个子节点如果 mount 了另外 tree
            // 则用另外一个 tree 的逻辑来查找
            if (nd[0].isMount()) {
                WnTree mntTree = factory().check(nd[0]);
                return mntTree.fetch(null, paths, i + 1, toIndex);
            }

        }
        return nd[0];
    }

    @Override
    public WnNode create(WnNode p, String path, WnRace race) {
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
        return create(p, paths, 0, len, race);

    }

    @Override
    public WnNode create(WnNode p, String[] paths, int fromIndex, int toIndex, WnRace race) {
        if (null == p) {
            p = treeNode;
        }

        // 创建所有的父
        WnNode nd = p;
        int i = fromIndex;
        for (; i < toIndex - 1; i++) {
            WnNode child = fetch(nd, paths, i, i + 1);
            // 有节点的话 ..
            if (null != child) {
                nd = child;
                // 如果节点挂载到了另外一颗树
                if (nd.isMount()) {
                    WnTree mntTree = factory().check(nd);
                    return mntTree.create(null, paths, i + 1, toIndex, race);
                }
                // 继续下一个路径
                continue;
            }
            // 没有节点，创建一个目录节点
            else {
                nd = create_node(nd, paths[i], WnRace.DIR);
            }
        }

        // 创建自己
        nd = create_node(nd, paths[i], race);

        // 更新缓存
        this._flush_buffer();

        return nd;
    }

    protected abstract WnNode create_node(WnNode p, String name, WnRace race);

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
