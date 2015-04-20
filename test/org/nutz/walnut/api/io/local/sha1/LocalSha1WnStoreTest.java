package org.nutz.walnut.api.io.local.sha1;

import org.nutz.walnut.api.io.AbstractWnStoreTest;

public class LocalSha1WnStoreTest extends AbstractWnStoreTest {

    @Override
    protected String getTreeMount() {
        return pp.check("mnt-mongo-a");
    }

}
