package org.nutz.walnut.impl.io.mongo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.nutz.walnut.BaseIndexerTest;
import org.nutz.walnut.Wnts;
import org.nutz.walnut.api.io.ObjIndexStrategy;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;

public class MongoWnIndexerTest extends BaseIndexerTest {

    @Test
    public void test_simple_get_set() {
        WnNode nd = tree.create(null, "/abc", WnRace.FILE);
        WnObj o = indexer.toObj(nd, ObjIndexStrategy.WC);
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

    @Override
    protected WnNode _create_top_tree_node() {
        return Wnts.create_tree_node(pp, "mnt-mongo-a");
    }

}
