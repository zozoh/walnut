package org.nutz.walnut.api.io.local.data;

import org.nutz.walnut.api.io.AbstractWnStoreTest;

public class LocalDataWnStoreTest extends AbstractWnStoreTest {

    @Override
    protected String getTreeMount() {
        return pp.check("mnt-mongo-x");
    }

}
