package org.nutz.walnut.impl.hook.local;

import org.nutz.walnut.Wnts;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.impl.hook.BaseIoWnHookTest;

public class LocalWnHookTest extends BaseIoWnHookTest {

    @Override
    protected WnNode _create_top_tree_node() {
        return Wnts.create_tree_node(pp, "mnt-local-a");
    }

}
