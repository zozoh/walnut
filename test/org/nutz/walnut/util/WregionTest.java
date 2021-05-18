package org.nutz.walnut.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class WregionTest {

    @Test
    public void test_extend_rg_in_offset() {
        String str = "[(2021-05-13)-30d, 2021-05-13]";
        String act = Wregion.extend_rg_macro(str);

        long am0 = Wtime.parseAMS("2021-04-13");
        long am1 = Wtime.parseAMS("2021-05-13");
        String exp = String.format("[%s,%s]", am0, am1);
        assertEquals(exp, act);
    }

    @Test
    public void test_extend_rg_in_ms() {
        String str = "[1607509999733,1613557999733]";
        String act = Wregion.extend_rg_macro(str);

        assertEquals(str, act);
    }

    @Test
    public void test_extend_rg_macro() {
        String str = "[2020-11-22 22:51:27, 2020-11-22 22:51:27+1d]";
        String act = Wregion.extend_rg_macro(str);
        assertEquals("[1606056687000,1606143087000]", act);

        str = "((2020-12-01)-3d, 2020-12-01]";
        act = Wregion.extend_rg_macro(str);
        System.out.println(act);
        assertEquals("(1606492800000,1606752000000]", act);
    }

    @Test
    public void test_int_region() {
        String regex = Wregion.intRegion();

        assertTrue("[43,]".matches(regex));
        assertTrue("[,23]".matches(regex));
        assertTrue("(551,999]".matches(regex));
        assertTrue("(887)".matches(regex));
        assertFalse("[rrr)".matches(regex));
        assertFalse("32,77".matches(regex));
    }

    @Test
    public void test_float_region() {
        String regex = Wregion.floatRegion();

        assertTrue("[.43,]".matches(regex));
        assertTrue("[,8.23]".matches(regex));
        assertTrue("(5.551,.999]".matches(regex));
        assertTrue("(7.887)".matches(regex));
        assertFalse("[800)".matches(regex));
        assertFalse("32,77".matches(regex));
    }

    @Test
    public void test_date_region() {
        String regex = Wregion.dateRegion("^");

        assertTrue("[1977-09-21,]".matches(regex));
        assertTrue("[,1977-09-21 08:23:11]".matches(regex));
        assertTrue("(1977-09-21 08:23:11,2015-10-18]".matches(regex));
        assertTrue("(1977-09-21)".matches(regex));
        assertFalse("[800)".matches(regex));
        assertFalse("32,77".matches(regex));
    }

}
