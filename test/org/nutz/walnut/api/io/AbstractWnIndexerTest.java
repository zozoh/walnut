package org.nutz.walnut.api.io;

import static org.junit.Assert.*;

import org.junit.Test;

public abstract class AbstractWnIndexerTest extends AbstractWnApiTest {

    @Test
    public void test_simple_get_set() {
        indexer.setValue("abc", "x", 100);
        indexer.setValue("abc", "y", 80);

        WnObj o = indexer.getBy("abc");
        assertEquals(100, o.getInt("x"));
        assertEquals(80, o.getInt("y"));
    }

}
