package com.site0.walnut.cheap.dom.selector;

import static org.junit.Assert.*;

import org.junit.Test;

public class CheapAxisSelectorTest {

    @Test
    public void test_01() {
        String s0, s2;
        CheapAxisSelector sel;

        s0 = "a.big > .small[x] > [href='mm']";
        sel = new CheapAxisSelector(s0);
        s2 = sel.toString();
        assertEquals(s0, s2);
    }

    @Test
    public void test_00() {
        String s0, s2;
        CheapAxisSelector sel;

        s0 = "a > b > c";
        sel = new CheapAxisSelector(s0);
        s2 = sel.toString();
        assertEquals(s0, s2);
    }

}
