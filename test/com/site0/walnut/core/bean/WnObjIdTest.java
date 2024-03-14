package com.site0.walnut.core.bean;

import static org.junit.Assert.*;

import org.junit.Test;

public class WnObjIdTest {

    @Test
    public void test_0() {
        WnObjId oid = new WnObjId("abc");
        assertFalse(oid.hasHomeId());
        assertNull(oid.getHomeId());
        assertEquals("abc", oid.getMyId());
    }

    @Test
    public void test_1() {
        WnObjId oid = new WnObjId("xyz:abc");
        assertTrue(oid.hasHomeId());
        assertEquals("xyz", oid.getHomeId());
        assertEquals("abc", oid.getMyId());
    }

}
