package com.site0.walnut.util.tmpl;

import static org.junit.Assert.*;

import org.junit.Test;

public class WnTmplTokenTest {

    @Test
    public void test_simpleToken() {
        String s = "A${B}C${D}E";
        WnTmplToken[] tks = WnTmplToken.parseToArray(s);
        assertEquals(5, tks.length);
        assertEquals("<TEXT>: 'A'", tks[0].toString());
        assertEquals("<DYNAMIC#VAR>: 'B'", tks[1].toString());
        assertEquals("<TEXT>: 'C'", tks[2].toString());
        assertEquals("<DYNAMIC#VAR>: 'D'", tks[3].toString());
        assertEquals("<TEXT>: 'E'", tks[4].toString());
    }

    @Test
    public void test_t2() {
        String s = "A${{B}}C${{{D}}}E";
        WnTmplToken[] tks = WnTmplToken.parseToArray(s);
        assertEquals(5, tks.length);
        assertEquals("<TEXT>: 'A'", tks[0].toString());
        assertEquals("<DYNAMIC#VAR>: '{B}'", tks[1].toString());
        assertEquals("<TEXT>: 'C'", tks[2].toString());
        assertEquals("<DYNAMIC#VAR>: '{{D}}'", tks[3].toString());
        assertEquals("<TEXT>: 'E'", tks[4].toString());
    }

}
