package org.nutz.walnut.impl.io;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.api.io.WnTreeFactory;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public abstract class AbstractWnTree implements WnTree {

    private WnNode treeNode;

    private WnTreeFactory factory;

    protected String rootPath;

    public AbstractWnTree(WnTreeFactory factory) {
        this.factory = factory;
    }

    @Override
    public WnNode getTreeNode() {
        return treeNode.clone();
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
    public boolean isTreeNode(String id) {
        return treeNode.isSameId(id);
    }

    public String getMount() {
        return treeNode.mount();
    }

    @Override
    public WnNode setMount(WnNode nd, String mnt) {
        // 不能自己挂载自己
        if (null != mnt && mnt.equals(treeNode.mount())) {
            throw Er.create("e.io.tree.mountself", mnt);
        }
        try {
            nd.loadParents(null, false);
            WnNode re = _do_set_mount(nd, mnt);
            this._flush_buffer();
            re.setTree(this);
            re.path(nd.path());
            re.mount(mnt);
            return re;
        }
        finally {
            _flush_buffer();
        }
    }

    protected abstract WnNode _do_set_mount(WnNode nd, String mnt);

    @Override
    public WnTreeFactory factory() {
        return factory;
    }

    @Override
    public void _clean_for_unit_test() {}

    @Override
    public WnNode getNode(final String id) {
        // 如果获取树根节点，直接返回
        if (this.isTreeNode(id))
            return getTreeNode();

        // 准备查找吧
        final WnNode[] nd = new WnNode[1];

        // 先找自己
        nd[0] = _get_my_node(id);

        // 没有的话，从自己的子树里寻找
        if (null == nd[0]) {
            eachMountTree(new Each<WnTree>() {
                public void invoke(int index, WnTree tree, int length) {
                    nd[0] = tree.getNode(id);
                    if (null != nd[0]) {
                        nd[0].setTree(tree);
                        Lang.Break();
                    }
                }
            });
        }
        // 如果有设置一下自己的树对象
        else {
            nd[0].setTree(this);
        }

        // 返回吧
        return nd[0];
    }

    protected abstract WnNode _get_my_node(String id);

    private void __assert_parent_can_create(WnNode p, WnRace race) {
        // 指定的节点，看看层级关系

        // 文件下面不能再有子对象
        if (p.isFILE()) {
            throw Er.create("e.io.tree.nd.file_as_parent", p);
        }
        // 对象下面只能有对象
        if (p.isOBJ() && race == WnRace.OBJ) {
            throw Er.create("e.io.tree.nd.child_must_obj", p);
        }
        // 必须在树内，否则不能创建，当然 Mount 节点树是父树，同时也是子树的根节点，需要除外
        if (!p.tree().equals(this)) {
            if (!this.isTreeNode(p.id()))
                throw Er.create("e.io.tree.nd.OutOfMount", p + " <!> " + treeNode);
        }
    }

    protected abstract int _each_children(WnNode p, String str, Each<WnNode> callback);

    @Override
    public int eachChildren(WnNode p, String str, final Each<WnNode> each) {
        if (null == p) {
            p = treeNode.clone();
        }

        // 读取所有的父节点
        p.loadParents(null, false);

        // 得到节点检查的回调接口
        final WnContext wc = Wn.WC();
        p = wc.whenEnter(p);

        final WnTree myTree = this;
        final WnNode pnd = p;

        // 开始循环
        final int[] re = new int[1];
        _each_children(p, str, new Each<WnNode>() {
            public void invoke(int index, WnNode nd, int length) {
                // 设置父
                nd.setTree(myTree);
                nd.setParent(pnd);
                nd.path(pnd.path()).appendPath(nd.name());

                // 不可见，就忽略
                nd = wc.whenView(nd);
                if (null == nd)
                    return;

                // 调用回调并计数
                each.invoke(re[0], nd, -1);
                re[0]++;
            }
        });
        return re[0];
    }

    @Override
    public WnNode fetch(WnNode p, String path) {
        if (path.startsWith("/")) {
            p = null;
        }
        String[] ss = Strings.splitIgnoreBlank(path, "[/]");
        return fetch(p, ss, 0, ss.length);
    }

    @Override
    public WnNode fetch(WnNode p, String[] paths, int fromIndex, int toIndex) {
        // 判断绝对路径
        if (null == p) {
            p = getTreeNode();
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
        WnNode nd;
        int rightIndex = toIndex - 1;
        for (int i = fromIndex; i < rightIndex; i++) {
            // 找子节点，找不到，就返回 null
            nd = this._fetch_one_by_name(p, paths[i]);
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

            // 如果找到了子节点，这个子节点如果 mount 了另外 tree
            // 则用另外一个 tree 的逻辑来查找
            if (nd.isMount(this)) {
                // 用尽路径元素了，则直接返回
                // zzh: 这个是一个优化，提前做点判断，就不用再递归到函数里面再判断了
                if (i + 1 >= toIndex) {
                    return nd;
                }
                // 用另外一颗树的逻辑来消费剩下的路径
                WnTree mntTree = factory().check(nd);
                return mntTree.fetch(null, paths, i + 1, toIndex);
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
        if (null != secu)
            nd = secu.access(nd);

        // 搞定了，返回吧
        return nd;
    }

    protected abstract WnNode _fetch_one_by_name(WnNode p, String name);

    public WnNode append(WnNode p, WnNode nd) {
        if (null == p)
            p = treeNode.clone();

        // 只有同树的节点才能转移
        p.assertTree(nd.tree());

        // 要移动的节点必须不能是顶级节点
        if (!nd.hasParent()) {
            throw Er.create("e.io.tree.appendRoot", nd);
        }

        // 看看有没有必要移动
        if (nd.isMyParent(p)) {
            return nd;
        }

        // 得到节点检查的回调接口
        WnSecurity secu = Wn.WC().getSecurity();

        // 分别检查节点
        if (null != secu) {
            p = secu.write(p);
            secu.write(nd.parent());
        }

        // 如果重名，则禁止移动
        if (null != this.fetch(p, nd.name())) {
            throw Er.create("e.io.tree.exists", nd.name());
        }

        // 执行移动
        WnNode newNode = _do_append(p, nd);
        _flush_buffer();

        // 返回
        newNode.setTree(nd.tree());
        newNode.path(p.path() + "/" + nd.name());
        return newNode;
    }

    protected abstract WnNode _do_append(WnNode p, WnNode nd);

    @Override
    public WnNode create(WnNode p, String path, WnRace race) {
        // 是否从树的根部创建
        if (path.startsWith("/")) {
            p = getTreeNode();
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
            p = getTreeNode();
        }

        // 加载父节点所有祖先
        p.loadParents(null, false);

        // 如果节点挂载到了另外一颗树
        if (p.isMount(this)) {
            WnTree mntTree = factory().check(p);
            if (mntTree.equals(this))
                throw Lang.impossible();
            return mntTree.create(p, paths, fromIndex, toIndex, race);
        }

        // 创建所有的父
        WnNode nd;
        int rightIndex = toIndex - 1;
        for (int i = fromIndex; i < rightIndex; i++) {
            nd = fetch(p, paths, i, i + 1);
            // 有节点的话 ..
            if (null != nd) {
                // 如果节点挂载到了另外一颗树
                if (nd.isMount(this)) {
                    WnTree mntTree = factory().check(nd);
                    if (mntTree.equals(this))
                        throw Lang.impossible();
                    return mntTree.create(nd, paths, i + 1, toIndex, race);
                }
                // 继续下一个路径
                p = nd;
                continue;
            }
            // 没有节点，创建一系列目录节点Ï
            for (; i < rightIndex; i++) {
                nd = _create_node(p, null, paths[i], WnRace.DIR);
                nd.setTree(this);
                nd.setParent(p);
                nd.path(p.path()).appendPath(nd.name());
                p = nd;
            }
        }

        // 创建自身节点
        return createNode(p, null, paths[rightIndex], race);
    }

    protected abstract WnNode _create_node(WnNode p, String id, String name, WnRace race);

    @Override
    public WnNode createNode(WnNode p, String id, String name, WnRace race) {
        // 得到节点检查的回调接口
        WnSecurity secu = Wn.WC().getSecurity();

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
        WnNode nd = _create_node(p, null, name, race);
        nd.setTree(this);
        nd.setParent(p);
        nd.path(p.path()).appendPath(nd.name());

        // 更新缓存并返回
        this._flush_buffer();
        return nd;
    }

    private void __assert_duplicate_name(WnNode p, String name) {
        if (exists(p, name))
            throw Er.create("e.io.exists", p);
    }

    @Override
    public WnNode rename(WnNode nd, String newName) {
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
        _flush_buffer();

        // 因为不确定子类会不会修改路径和名称，最后统一修改节点的 nm 和 path
        nd.name(newName);
        String ph = nd.path();
        if (!Strings.isBlank(ph)) {
            nd.path(Files.renamePath(ph, newName));
        }

        // 返回
        return nd;
    }

    protected abstract WnNode _do_rename(WnNode nd, String newName);

    protected void _do_walk_children(WnNode p, final Callback<WnNode> callback) {
        this.eachChildren(p, null, new Each<WnNode>() {
            public void invoke(int index, WnNode nd, int length) {
                callback.invoke(nd);
            }
        });
    }

    @Override
    public void walk(WnNode p, final Callback<WnNode> callback, final WalkMode mode) {

        if (null != p)
            System.out.println("walk: " + p.name());

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

    private void __walk_LEAF_ONLY(WnNode p, final Callback<WnNode> callback) {
        _do_walk_children(p, new Callback<WnNode>() {
            public void invoke(WnNode nd) {
                if (nd.isFILE() || nd.isOBJ())
                    callback.invoke(nd);
                else
                    __walk_LEAF_ONLY(nd, callback);
            }
        });
    }

    private void __walk_BREADTH_FIRST(WnNode p, final Callback<WnNode> callback) {
        final List<WnNode> list = new LinkedList<WnNode>();
        _do_walk_children(p, new Callback<WnNode>() {
            public void invoke(WnNode nd) {
                callback.invoke(nd);
                if (!nd.isFILE())
                    list.add(nd);
            }
        });
        for (WnNode nd : list)
            __walk_BREADTH_FIRST(nd, callback);
    }

    private void __walk_DEPATH_NODE_FIRST(WnNode p, final Callback<WnNode> callback) {
        _do_walk_children(p, new Callback<WnNode>() {
            public void invoke(WnNode nd) {
                callback.invoke(nd);
                if (!nd.isFILE()) {
                    __walk_DEPATH_NODE_FIRST(nd, callback);
                }
            }
        });
    }

    private void __walk_DEPTH_LEAF_FIRST(WnNode p, final Callback<WnNode> callback) {
        _do_walk_children(p, new Callback<WnNode>() {
            public void invoke(WnNode nd) {
                if (!nd.isFILE()) {
                    __walk_DEPTH_LEAF_FIRST(nd, callback);
                }
                callback.invoke(nd);
            }
        });
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
        _delete_self(nd);

        // 更新缓存
        _flush_buffer();
    }

    protected abstract void _delete_self(WnNode nd);

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
