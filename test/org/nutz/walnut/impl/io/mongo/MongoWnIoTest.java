package org.nutz.walnut.impl.io.mongo;

import org.nutz.walnut.Wnts;
import org.nutz.walnut.api.io.AbstractWnIoTest;
import org.nutz.walnut.api.io.WnNode;

public class MongoWnIoTest extends AbstractWnIoTest {

    @Override
    protected String getAnotherTreeMount() {
        return pp.check("mnt-local-a");
    }

    @Override
    protected WnNode _create_top_tree_node() {
        return Wnts.create_tree_node(pp,"mnt-mongo-a");
    }
}
