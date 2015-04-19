package org.nutz.walnut.api.io.mongo;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.api.io.AbstractWnIndexerTest;
import org.nutz.walnut.impl.mongo.MongoWnIndexer;

public class MongoWnIndexerTest extends AbstractWnIndexerTest {

    protected void on_before(PropertiesProxy pp) {
        ZMoCo co = db.getCollectionByMount("mongo:obj");
        indexer = new MongoWnIndexer(co);
        indexer._clean_for_unit_test();
    }

}
