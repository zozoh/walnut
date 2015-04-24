package org.nutz.walnut.impl;

import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnTree;

public class WnSecurityImpl implements WnSecurity {

    WnIndexer indexer;

    WnTree tree;

    public WnSecurityImpl(WnIndexer indexer, WnTree tree) {
        this.indexer = indexer;
        this.tree = tree;
    }

    @Override
    public <T extends WnNode> T enter(T nd) {

        return null;
    }

    @Override
    public <T extends WnNode> T access(T nd) {
        return null;
    }

    @Override
    public <T extends WnNode> T view(T nd) {
        return null;
    }

    @Override
    public <T extends WnNode> T read(T nd) {
        return null;
    }

    @Override
    public <T extends WnNode> T write(T nd) {
        return null;
    }

}
// // 检查基本权限
// int mode = last.mode();
//
// // 检查 other
// int m = mode & 7;
// if (!Maths.isMaskAll(m, mode))
// throw Er.create("e.io.forbiden", nd);
//
// // 检查 member
// m = mode >> 3 & 7;
// if (!Maths.isMaskAll(m, mode))
// throw Er.create("e.io.forbiden", nd);
//
// // 检查 admin
// m = mode >> 6 & 7;
// if (!Maths.isMaskAll(m, mode))
// throw Er.create("e.io.forbiden", nd);

//