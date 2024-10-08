package com.site0.walnut.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.nutz.lang.util.NutMap;

public class WnSortTest {

    @Test
    public void test_0() {
        NutMap o1 = Wlang.map("nm:'a', age: 10");
        NutMap o2 = Wlang.map("nm:'b', age: 10");

        WnSort[] sorts = WnSort.makeList("nm:1");
        assertEquals(-1, WnSort.compare(sorts, o1, o2));
        assertEquals(1, WnSort.compare(sorts, o2, o1));
    }

    @Test
    public void test_1() {
        NutMap o1 = Wlang.map("nm:'a', age: 10");
        NutMap o2 = Wlang.map("nm:'b', age: 10");

        WnSort[] sorts = WnSort.makeList("age:1");
        assertEquals(0, WnSort.compare(sorts, o1, o2));
        assertEquals(0, WnSort.compare(sorts, o2, o1));
    }

    @Test
    public void test_2() {
        NutMap o1 = Wlang.map("nm:'a', age: 10");
        NutMap o2 = Wlang.map("nm:'b', age: 10");

        WnSort[] sorts = WnSort.makeList("age:1, nm:1");
        assertEquals(-1, WnSort.compare(sorts, o1, o2));
        assertEquals(1, WnSort.compare(sorts, o2, o1));

        sorts = WnSort.makeList("age:1, nm:-1");
        assertEquals(1, WnSort.compare(sorts, o1, o2));
        assertEquals(-1, WnSort.compare(sorts, o2, o1));
    }

}
