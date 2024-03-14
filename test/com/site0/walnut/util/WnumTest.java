package com.site0.walnut.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class WnumTest {

    @Test
    public void testScrollIndex() {
        assertEquals(3, Wnum.scrollIndex(3, 5));
        assertEquals(0, Wnum.scrollIndex(0, 5));
        assertEquals(4, Wnum.scrollIndex(4, 5));
        assertEquals(0, Wnum.scrollIndex(5, 5));
        assertEquals(1, Wnum.scrollIndex(6, 5));
        assertEquals(4, Wnum.scrollIndex(-1, 5));
        assertEquals(0, Wnum.scrollIndex(-5, 5));
        assertEquals(4, Wnum.scrollIndex(-6, 5));
        assertEquals(0, Wnum.scrollIndex(-5, 5));
    }

}
