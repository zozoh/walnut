package org.nutz.lang.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.Times;

public class ValueRangeTest {

    @Test
    public void test_gt_lt() {
        assertFalse(Ranges.Int("(,2)").match(3));
        assertTrue(Ranges.Int("(1,2]").match(2));
        assertTrue(Ranges.Int("[2,)").match(3));
    }

    @Test
    public void test_equals() {
        assertFalse(Ranges.Int("[2)").match(1));
        assertTrue(Ranges.Int("[2]").match(2));
        assertFalse(Ranges.Int("(2]").match(3));
        assertTrue(Ranges.Int("(2)").match(3));
        assertFalse(Ranges.Int("(2)").match(2));
        assertTrue(Ranges.Int("(2)").match(1));
    }

    @Test
    public void test_int_regin() {
        assertFalse(Ranges.Int("(1,3)").match(1));
        assertTrue(Ranges.Int("(1,3)").match(2));
        assertFalse(Ranges.Int("(1,3)").match(3));

        assertFalse(Ranges.Int("[1,3]").match(-1));
        assertTrue(Ranges.Int("[1,3]").match(1));
        assertTrue(Ranges.Int("[1,3]").match(2));
        assertTrue(Ranges.Int("[1,3]").match(3));
        assertFalse(Ranges.Int("[1,3]").match(4));
    }

    @Test
    public void test_int_date() {
        assertFalse(Ranges.Date("(2013-9-20,2013-9-22)").match(Times.D("2013-9-20")));
        assertTrue(Ranges.Date("(2013-9-20,2013-9-22)").match(Times.D("2013-9-21")));
        assertFalse(Ranges.Date("(2013-9-20,2013-9-22)").match(Times.D("2013-9-22")));

        assertFalse(Ranges.Date("[2013-9-20,2013-9-22]").match(Times.D("2013-9-19")));
        assertTrue(Ranges.Date("[2013-9-20,2013-9-22]").match(Times.D("2013-9-20")));
        assertTrue(Ranges.Date("[2013-9-20,2013-9-22]").match(Times.D("2013-9-21")));
        assertTrue(Ranges.Date("[2013-9-20,2013-9-22]").match(Times.D("2013-9-22")));
        assertFalse(Ranges.Date("[2013-9-20,2013-9-22]").match(Times.D("2013-9-23")));
    }

    @Test
    public void test_auto_swap() {
        assertFalse(Ranges.Int("(3,1)").match(1));
        assertTrue(Ranges.Int("(3,1)").match(2));
        assertFalse(Ranges.Int("(3,1)").match(3));
    }
}
