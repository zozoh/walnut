package org.nutz.walnut.ext.data.fake.impl;

import static org.junit.Assert.*;

import org.junit.Test;

public class WnIntegerFakerTest {

    @Test
    public void test_00() {
        WnIntegerFaker fa = new WnIntegerFaker(0, 100);
        for (int i = 0; i < 100; i++) {
            int n = fa.next();
            assertTrue(n >= 0 && n < 100);
            assertFalse(n < 0 || n >= 100);
        }
    }

}
