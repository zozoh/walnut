package org.nutz.walnut.core.indexer.mongo;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.util.Region;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.util.Wn;

import com.mongodb.DBObject;

public class MongosTest {

    @Test
    public void test_int_range() {
        WnQuery q = Wn.Q.pid("--pid--");
        q.setv("nb", Region.Int("[1,)"));
        ZMoDoc doc = Mongos.toQueryDoc(q);

        assertEquals("--pid--", doc.get("pid"));

        DBObject cNB = (DBObject) doc.get("nb");
        assertEquals(1, cNB.keySet().size());
        assertEquals(1, cNB.get("$gte"));
    }

    @Test
    public void test_int_range_s() {
        WnQuery q = Wn.Q.pid("--pid--");
        q.setv("nb", "[1,)");
        ZMoDoc doc = Mongos.toQueryDoc(q);

        assertEquals("--pid--", doc.get("pid"));

        DBObject cNB = (DBObject) doc.get("nb");
        assertEquals(1, cNB.keySet().size());
        assertEquals(1, cNB.get("$gte"));
    }

}
