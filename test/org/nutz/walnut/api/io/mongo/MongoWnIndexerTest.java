package org.nutz.walnut.api.io.mongo;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Mirror;
import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.api.io.AbstractWnIndexerTest;
import org.nutz.walnut.impl.mongo.MongoDB;
import org.nutz.walnut.impl.mongo.MongoWnIndexer;

public class MongoWnIndexerTest extends AbstractWnIndexerTest {

    protected void on_before(PropertiesProxy pp) {
        MongoDB db = new MongoDB();
        Mirror.me(db).setValue(db, "host", pp.get("mongo-host"));
        Mirror.me(db).setValue(db, "port", pp.getInt("mongo-port"));
        Mirror.me(db).setValue(db, "usr", pp.get("mongo-usr"));
        Mirror.me(db).setValue(db, "pwd", pp.get("mongo-pwd"));
        Mirror.me(db).setValue(db, "db", pp.get("mongo-db"));
        db.on_create();
        ZMoCo co = db.db().cc("obj", true);
        indexer = new MongoWnIndexer(co);
    }

}
