package org.nutz.walnut.impl.hook.mongo;

import org.nutz.walnut.Wnts;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.impl.hook.BaseIoWnHookTest;

public class MongoWnHookTest extends BaseIoWnHookTest {

    @Override
    protected WnNode _create_top_tree_node() {
        return Wnts.create_tree_node(pp, "mnt-mongo-a");
    }

}
