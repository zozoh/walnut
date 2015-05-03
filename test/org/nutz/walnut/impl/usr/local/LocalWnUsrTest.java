package org.nutz.walnut.impl.usr.local;

import org.nutz.walnut.Wnts;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.impl.usr.BaseIoWnUsrTest;

public class LocalWnUsrTest extends BaseIoWnUsrTest {

    @Override
    protected WnNode _create_top_tree_node() {
        return Wnts.create_tree_node(pp, "mnt-local-a");
    }

}
