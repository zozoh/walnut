package org.nutz.walnut;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.walnut.api.io.WnStore;
import org.nutz.walnut.impl.io.WnStoreFactoryImpl;

public abstract class BaseStoreTest extends BaseIndexerTest {

    protected WnStore store;

    protected void on_before(PropertiesProxy pp) {
        super.on_before(pp);

        storeFactory = new WnStoreFactoryImpl(indexer,
                                              db,
                                              pp.check("local-sha1"),
                                              pp.check("local-data"));
        store = storeFactory.get(tree.getTreeNode());
        store._clean_for_unit_test();
    }

}
