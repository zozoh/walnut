package org.nutz.walnut.impl;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.util.Wn;

public abstract class AbstractWnNode implements WnNode {

    @Override
    public WnNode genID() {
        return id(Wn.genId());
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
            throw Er.create("e.io.tree.nd.OutOfMount", this + " <!> " + tree);
        }
    }

    @Override
    public String toString() {
        return String.format("%s:%s", id(), name());
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

}
