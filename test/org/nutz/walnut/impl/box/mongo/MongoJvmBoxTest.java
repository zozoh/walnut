package org.nutz.walnut.impl.box.mongo;

import org.nutz.walnut.Wnts;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.impl.box.BaseJvmBoxTest;

public class MongoJvmBoxTest extends BaseJvmBoxTest {

    @Override
    protected WnNode _create_top_tree_node() {
        return Wnts.create_tree_node(pp, "mnt-mongo-a");
    }

}
