package com.site0.walnut.core.indexer.mongo;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.util.Region;
import org.nutz.mongo.ZMoDoc;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.util.Wn;

public class MongosTest {

    @Test
    public void test_int_range() {
        WnQuery q = Wn.Q.pid("--pid--");
        q.setv("nb", Region.Int("[1,)"));
        ZMoDoc doc = Mongos.toQueryDoc(q);

        assertEquals("--pid--", doc.get("pid"));

        ZMoDoc cNB = (ZMoDoc) doc.get("nb");
        assertEquals(1, cNB.keySet().size());
        assertEquals(1, cNB.get("$gte"));
    }

    @Test
    public void test_int_range_s() {
        WnQuery q = Wn.Q.pid("--pid--");
        q.setv("nb", "[1,)");
        ZMoDoc doc = Mongos.toQueryDoc(q);

        assertEquals("--pid--", doc.get("pid"));

        ZMoDoc cNB = (ZMoDoc) doc.get("nb");
        assertEquals(1, cNB.keySet().size());
        assertEquals(1, cNB.get("$gte"));
    }

}
