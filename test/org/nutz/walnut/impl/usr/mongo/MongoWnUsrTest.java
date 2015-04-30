package org.nutz.walnut.impl.usr.mongo;

import org.nutz.walnut.WnTUs;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.impl.usr.AbstractIoWnUsrTest;

public class MongoWnUsrTest extends AbstractIoWnUsrTest {

    @Override
    protected WnNode _create_top_tree_node() {
        return WnTUs.create_tree_node(pp, "mnt-mongo-a");
    }

}
