package org.nutz.walnut.api.io;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.impl.WnTreeFactoryImpl;
import org.nutz.walnut.impl.mongo.MongoWnIndexer;

public abstract class AbstractWnIndexerTest extends AbstractWnApiTest {

    @Test
    public void test_simple_get_set() {
        WnNode nd = tree.create(null, "/abc", WnRace.FILE);
        WnObj o = indexer.toObj(nd);
        o.setv("x", 100).setv("y", 80).setv("z", 9000);
        indexer.set(o, "^x|y$");

        assertEquals(100, o.getInt("x"));
        assertEquals(80, o.getInt("y"));
        assertEquals(9000, o.getInt("z"));

        o = indexer.get(nd.id());
        assertEquals(100, o.getInt("x"));
        assertEquals(80, o.getInt("y"));
        assertEquals(-1, o.getInt("z"));
    }

    protected WnTree tree;

    protected void on_before(PropertiesProxy pp) {
        treeFactory = new WnTreeFactoryImpl(db);

        tree = treeFactory.check("", getTreeMount());
        tree._clean_for_unit_test();

        ZMoCo co = db.getCollectionByMount("mongo:obj");
        indexer = new MongoWnIndexer(co);
        indexer._clean_for_unit_test();
    }

    protected String getTreeMount() {
        return pp.check("mnt-mongo-x");
    }
}
