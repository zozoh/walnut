package org.nutz.walnut.impl.io.mongo;

import org.nutz.walnut.Wnts;
import org.nutz.walnut.api.io.AbstractWnTreeTest;
import org.nutz.walnut.api.io.WnNode;

public class MongoWnTreeTest extends AbstractWnTreeTest {

    @Override
    protected WnNode _create_top_tree_node() {
        return Wnts.create_tree_node(pp,"mnt-mongo-a");
    }

    @Override
    protected String _another_tree_mount_key() {
        return "mnt-local-a";
    }

}
