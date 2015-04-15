package org.nutz.walnut.api.io;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.walnut.api.io.WnObj;

public abstract class AbstractWnIndexerTest extends AbstractWnApiTest {

    @Test
    public void test_simple_get_set() {
        indexer.set("abc", "x", 100);
        indexer.set("abc", "y", 80);

        WnObj o = indexer.get("abc");
        assertEquals(100, o.getInt("x"));
        assertEquals(80, o.getInt("y"));
    }

}
