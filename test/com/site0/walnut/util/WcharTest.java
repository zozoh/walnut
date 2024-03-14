package com.site0.walnut.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class WcharTest {

    @Test
    public void test_EscapeTable() {
        char[] cs = Wchar.array(';', ';', '\r', ' ', '\n', ' ');
        Wchar.EscapeTable tab = Wchar.buildEscapeTable(cs);

        assertEquals(';', tab.get(';'));
        assertEquals(' ', tab.get('\r'));
        assertEquals(' ', tab.get('\n'));
        assertEquals(0, tab.get('a'));
        assertEquals(0, tab.get('b'));
        assertEquals(0, tab.get('c'));
        assertEquals(0, tab.get('d'));
    }

}
