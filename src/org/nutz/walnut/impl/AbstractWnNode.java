package org.nutz.walnut.impl;

import org.nutz.lang.Strings;
import org.nutz.mongo.annotation.MoIgnore;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.util.Wn;

public abstract class AbstractWnNode implements WnNode {

    @MoIgnore
    private WnNode parent;

    @MoIgnore
    private WnTree tree;

    @MoIgnore
    private String path;

    public WnTree tree() {
        return tree;
    }

    public void setTree(WnTree tree) {
        this.tree = tree;
    }

    @Override
    public boolean hasParent() {
        return null != parent;
    }

    @Override
    public WnNode parent() {
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
    public WnNode path(String path) {
        this.path = path;
        return this;
    }

    @Override
    public boolean hasID() {
        return !Strings.isBlank(id());
    }

    @Override
    public WnNode genID() {
        id(Wn.genId());
        return this;
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
        if (!isMount())
            return String.format("%s:%s", id(), name());

        return String.format("%s:%s >> %s", id(), name(), mount());
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
