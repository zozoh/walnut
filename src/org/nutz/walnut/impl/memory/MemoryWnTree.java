package org.nutz.walnut.impl.memory;

import org.nutz.lang.Each;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnTreeFactory;
import org.nutz.walnut.impl.AbstractWnTree;

public class MemoryWnTree extends AbstractWnTree {

    public MemoryWnTree(WnTreeFactory factory, WnNode treeNode) {
        super(factory, treeNode);
    }

    @Override
    public WnNode getNode(String id) {
        return null;
    }

    @Override
    public WnNode loadParents(WnNode nd, boolean force) {
        return null;
    }

    @Override
    public int eachChildren(WnNode p, String str, Each<WnNode> callback) {
        return 0;
    }

    @Override
    public boolean hasChildren(WnNode nd) {
        return false;
    }

    @Override
    public void rename(WnNode nd, String newName) {}

    @Override
    public void setMount(WnNode nd, String mnt) {}

    @Override
    public void _clean_for_unit_test() {}

    @Override
    protected WnNode create_node(WnNode p, String name, WnRace race) {
        return null;
    }

    @Override
    protected void delete_self(WnNode nd) {}

    @Override
    protected void _flush_buffer() {}

}
