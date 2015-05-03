package org.nutz.walnut.impl.io.local;

import org.nutz.walnut.Wnts;
import org.nutz.walnut.api.io.AbstractWnIoTest;
import org.nutz.walnut.api.io.WnNode;

public class LocalWnIoTest extends AbstractWnIoTest {

    @Override
    protected WnNode _create_top_tree_node() {
        return Wnts.create_tree_node(pp, "mnt-local-a");
    }

    @Override
    protected String getAnotherTreeMount() {
        return pp.check("mnt-mongo-a");
    }
}
