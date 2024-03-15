package com.site0.walnut.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class ZParamsTest {

    @Test
    public void test_pairs() {
        String[] args = Wlang.array("-src", "AAA", "-ok", "-ta", "haha");
        ZParams params = ZParams.parse(args, null);
        assertEquals(0, params.vals.length);

        assertTrue(params.is("ok"));
        assertEquals("AAA", params.get("src"));
        assertEquals("haha", params.get("ta"));
    }

    @Test
    public void test_boolstr() {
        String[] args = Wlang.array("-read", "-same", "hello");
        ZParams params = ZParams.parse(args, "^read|same$");
        assertEquals(1, params.vals.length);
        assertEquals("hello", params.vals[0]);

        assertTrue(params.is("read"));
        assertTrue(params.is("same"));
    }

    @Test
    public void test_boolchars() {
        String[] args = Wlang.array("abc", "-avl", "hello");
        ZParams params = ZParams.parse(args, "avl");
        assertEquals(2, params.vals.length);
        assertEquals("abc", params.vals[0]);
        assertEquals("hello", params.vals[1]);

        assertTrue(params.is("a"));
        assertTrue(params.is("v"));
        assertTrue(params.is("l"));
    }

    @Test
    public void test_simple() {
        String[] args = Wlang.array("A", "-u", "B");
        ZParams params = ZParams.parse(args, null);
        assertEquals(1, params.vals.length);
        assertEquals("A", params.vals[0]);

        assertTrue(params.has("u"));
        assertEquals("B", params.get("u"));
    }

}
