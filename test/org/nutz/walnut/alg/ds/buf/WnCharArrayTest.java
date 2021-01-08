package org.nutz.walnut.alg.ds.buf;

import static org.junit.Assert.*;

import org.junit.Test;

public class WnCharArrayTest {

    @Test
    public void test_read_line_chars() {
        String s = "ABC\n";
        s += "XYZ\n";
        s += "123\n";
        WnCharArray wca = new WnCharArray(s);

        assertEquals("ABC", new String(wca.nextLineChars()));
        assertEquals("XYZ", new String(wca.nextLineChars()));
        assertEquals("123", new String(wca.nextLineChars()));
        assertEquals(null, wca.nextLineChars());
        assertEquals(null, wca.nextLineChars());
    }

    @Test
    public void test_read_line() {
        String s = "ABC\n";
        s += "XYZ\n";
        s += "123\n";
        WnCharArray wca = new WnCharArray(s);

        assertEquals("ABC", wca.nextLine());
        assertEquals("XYZ", wca.nextLine());
        assertEquals("123", wca.nextLine());
        assertEquals(null, wca.nextLine());
        assertEquals(null, wca.nextLine());
    }

    @Test
    public void test_next_prev() {
        WnCharArray wca = new WnCharArray("abcde");

        assertTrue(wca.hasNextChar());
        assertEquals('a', wca.nextChar());

        assertTrue(wca.hasNextChar());
        assertEquals('b', wca.nextChar());

        assertTrue(wca.hasNextChar());
        assertEquals('c', wca.nextChar());

        assertTrue(wca.hasNextChar());
        assertEquals('d', wca.nextChar());

        assertTrue(wca.hasNextChar());
        assertEquals('e', wca.nextChar());

        assertFalse(wca.hasNextChar());
        assertEquals(0, wca.nextChar());
        assertEquals(0, wca.nextChar());

        assertTrue(wca.hasPrevChar());
        assertEquals('e', wca.prevChar());

        assertTrue(wca.hasPrevChar());
        assertEquals('d', wca.prevChar());

        assertTrue(wca.hasPrevChar());
        assertEquals('c', wca.prevChar());

        assertTrue(wca.hasPrevChar());
        assertEquals('b', wca.prevChar());

        assertTrue(wca.hasPrevChar());
        assertEquals('a', wca.prevChar());

        assertFalse(wca.hasPrevChar());
        assertEquals(0, wca.prevChar());
        assertEquals(0, wca.prevChar());
    }

}
