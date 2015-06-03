package org.nutz.walnut.impl.io;

import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.mongo.annotation.MoIgnore;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.util.Wn;

public abstract class AbstractWnNode implements WnNode {

    @MoIgnore
    protected WnNode parent;

    @MoIgnore
    private WnTree tree;

    @MoIgnore
    private String path;

    public WnTree tree() {
        return tree;
    }

    @Override
    public boolean isRootNode() {
        return tree.isTreeNode(id());
    }

    public void setTree(WnTree tree) {
        this.tree = tree;
    }

    @Override
    public boolean hasParent() {
        return !Strings.isBlank(parentId());
    }

    @Override
    public WnNode parent() {
        if (null == parent && hasParent()) {
            loadParents(null, false);
        }
        return parent;
    }

    @Override
    public WnNode loadParents(List<WnNode> list, boolean force) {
        // 已经加载过了，且不是强制加载，就啥也不干
        if (null != parent && !force) {
            if (Strings.isBlank(path)) {
                path(parent.path()).appendPath(name());
            }
            return parent;
        }

        // 如果自己就是树的根节点则表示到头了
        // 因为 Mount 的树，它的树对象是父树
        WnTree myTree = tree();
        if (myTree.isTreeNode(id())) {
            path("/");
            return this;
        }

        // 得到父节点
        String pid = parentId();
        WnNode p = myTree.getNode(pid);

        // 没有父，是不可能的
        if (null == p) {
            throw Lang.impossible();
        }

        // 递归加载父节点的祖先
        p.loadParents(list, force);

        // 确保可访问
        p = Wn.WC().whenEnter(p);

        // 设置成自己的父
        parent = p;

        // 记录到输出列表
        if (null != list)
            list.add(parent);

        // 更新路径
        path(parent.path()).appendPath(name());

        // 返回父节点
        return parent;
    }

    @Override
    public String parentId() {
        return null != parent ? parent.id() : null;
    }

    @Override
    public void setParent(WnNode parent) {
        this.parent = parent;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String realPath() {
        return path;
    }

    @Override
    public WnNode path(String path) {
        this.path = path;
        return this;
    }

    @Override
    public WnNode appendPath(String ph) {
        path(Wn.appendPath(this.path, ph));
        return this;
    }

    @Override
    public boolean hasID() {
        return !Strings.isBlank(id());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WnNode) {
            WnNode nd = (WnNode) obj;
            return nd.id().equals(id()) && nd.name().equals(name());
        }
        return false;
    }

    @Override
    public void assertTree(WnTree tree) {
        if (!tree.equals(this.tree())) {
            throw Er.create("e.io.tree.nd.NotSameTree", this.tree + " <!> " + tree);
        }
    }

    @Override
    public String toString() {
        if (!isMount(tree()))
            return String.format("%s:%s", id(), name());

        return String.format("%s:%s >> %s", id(), name(), mount());
    }

    @Override
    public boolean isMount(WnTree myTree) {
        String mnt = mount();
        if (Strings.isBlank(mnt))
            return false;
        return !myTree.getMount().equals(mnt);
    }

    @Override
    public boolean isMyParent(WnNode p) {
        if (null == p)
            return false;

        return parentId().equals(p.id());
    }

    @Override
    public boolean isSameId(WnNode nd) {
        return isSameId(nd.id());
    }

    @Override
    public boolean isSameId(String id) {
        if (!hasID() || null == id)
            return false;
        return id().equals(id);
    }

    @Override
    public WnNode clone() {
        return duplicate();
    }

}
