package org.nutz.lang.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.Times;

public class ValueRangeTest {

    @Test
    public void test_gt_lt() {
        assertFalse(ValueRange.Int("(,2)").match(3));
        assertTrue(ValueRange.Int("(1,2]").match(2));
        assertTrue(ValueRange.Int("[2,)").match(3));
    }

    @Test
    public void test_equals() {
        assertFalse(ValueRange.Int("[2)").match(1));
        assertTrue(ValueRange.Int("[2]").match(2));
        assertFalse(ValueRange.Int("(2]").match(3));
        assertTrue(ValueRange.Int("(2)").match(3));
        assertFalse(ValueRange.Int("(2)").match(2));
        assertTrue(ValueRange.Int("(2)").match(1));
    }

    @Test
    public void test_int_regin() {
        assertFalse(ValueRange.Int("(1,3)").match(1));
        assertTrue(ValueRange.Int("(1,3)").match(2));
        assertFalse(ValueRange.Int("(1,3)").match(3));

        assertFalse(ValueRange.Int("[1,3]").match(-1));
        assertTrue(ValueRange.Int("[1,3]").match(1));
        assertTrue(ValueRange.Int("[1,3]").match(2));
        assertTrue(ValueRange.Int("[1,3]").match(3));
        assertFalse(ValueRange.Int("[1,3]").match(4));
    }

    @Test
    public void test_int_date() {
        assertFalse(ValueRange.Date("(2013-9-20,2013-9-22)").match(Times.D("2013-9-20")));
        assertTrue(ValueRange.Date("(2013-9-20,2013-9-22)").match(Times.D("2013-9-21")));
        assertFalse(ValueRange.Date("(2013-9-20,2013-9-22)").match(Times.D("2013-9-22")));

        assertFalse(ValueRange.Date("[2013-9-20,2013-9-22]").match(Times.D("2013-9-19")));
        assertTrue(ValueRange.Date("[2013-9-20,2013-9-22]").match(Times.D("2013-9-20")));
        assertTrue(ValueRange.Date("[2013-9-20,2013-9-22]").match(Times.D("2013-9-21")));
        assertTrue(ValueRange.Date("[2013-9-20,2013-9-22]").match(Times.D("2013-9-22")));
        assertFalse(ValueRange.Date("[2013-9-20,2013-9-22]").match(Times.D("2013-9-23")));
    }

    @Test
    public void test_auto_swap() {
        assertFalse(ValueRange.Int("(3,1)").match(1));
        assertTrue(ValueRange.Int("(3,1)").match(2));
        assertFalse(ValueRange.Int("(3,1)").match(3));
    }
}
