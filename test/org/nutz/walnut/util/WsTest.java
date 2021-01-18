package org.nutz.walnut.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class WsTest {

    @Test
    public void test_escape_unescape() {
        String input = "a\nb\r\tc";

        String s = Ws.escape(input);
        assertEquals("a\\nb\\r\\tc", s);

        String s2 = Ws.unescape(s);
        assertEquals(input, s2);
    }

}
