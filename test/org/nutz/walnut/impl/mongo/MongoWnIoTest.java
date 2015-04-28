package org.nutz.walnut.impl.mongo;

import org.nutz.walnut.WnTUs;
import org.nutz.walnut.api.io.AbstractWnIoTest;
import org.nutz.walnut.api.io.WnNode;

public class MongoWnIoTest extends AbstractWnIoTest {

    @Override
    protected String getAnotherTreeMount() {
        return pp.check("mnt-local-a");
    }

    @Override
    protected WnNode _create_top_tree_node() {
        return WnTUs.create_tree_node(pp,"mnt-mongo-a");
    }
}
