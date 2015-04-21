package org.nutz.walnut.api.io.local.tree;

import org.nutz.walnut.api.io.AbstractWnStoreTest;

public class LocalTreeWnStoreTest extends AbstractWnStoreTest {

    @Override
    protected String getTreeMount() {
        return pp.check("mnt-local-a");
    }

}
