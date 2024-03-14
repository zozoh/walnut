package com.site0.walnut.cheap.dom.selector;

import static org.junit.Assert.*;

import org.junit.Test;

public class CheapDomSelectorTest {

    @Test
    public void test_01() {
        String s0, s2;
        CheapSelector sel;

        s0 = "a > b > c, [href='#'], .xyz[a='56']";
        sel = new CheapDomSelector(s0);
        s2 = sel.toString();
        assertEquals(s0, s2);
    }

    @Test
    public void test_00() {
        String s0, s2;
        CheapSelector sel;

        s0 = "a > b > c";
        sel = new CheapDomSelector(s0);
        s2 = sel.toString();
        assertEquals(s0, s2);
    }

}
