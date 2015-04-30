package org.nutz.walnut.impl.usr.local;

import org.nutz.walnut.WnTUs;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.impl.usr.AbstractIoWnUsrTest;

public class LocalWnUsrTest extends AbstractIoWnUsrTest {

    @Override
    protected WnNode _create_top_tree_node() {
        return WnTUs.create_tree_node(pp, "mnt-local-a");
    }

}
