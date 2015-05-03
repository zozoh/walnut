package org.nutz.walnut.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.Lang;

public class ZParamsTest {

    @Test
    public void test() {
        ZParams params = ZParams.parse(Lang.array("A", "-u", "B"), null);
        assertEquals(1, params.vals.length);
        assertEquals("A", params.vals[0]);

        assertTrue(params.has("u"));
        assertEquals("B", params.get("u"));
    }

}
