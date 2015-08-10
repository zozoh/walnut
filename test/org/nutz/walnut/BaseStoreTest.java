package org.nutz.walnut;

import java.io.File;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Files;
import org.nutz.lang.Mirror;
import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.api.io.WnStore;
import org.nutz.walnut.impl.io.WnStoreImpl;
import org.nutz.walnut.impl.io.handle.WnHandleManagerImpl;
import org.nutz.walnut.impl.io.mongo.MongoLocalBucketManager;

public abstract class BaseStoreTest extends BaseApiTest {

    protected WnStore store;

    protected void on_before(PropertiesProxy pp) {
        super.on_before(pp);

        store = _create_store();
        store._clean_for_unit_test();
    }

    private WnStore _create_store() {
        store = new WnStoreImpl();

        ZMoCo co = db.getCollection(pp.get("bucket-colnm"));
        File home = Files.createDirIfNoExists(pp.get("bucket-home"));
        MongoLocalBucketManager buckets = new MongoLocalBucketManager(home, co);

        WnHandleManagerImpl handles = new WnHandleManagerImpl();

        Mirror.me(store).setValue(store, "buckets", buckets);
        Mirror.me(store).setValue(store, "handles", handles);

        return store;
    }
}
