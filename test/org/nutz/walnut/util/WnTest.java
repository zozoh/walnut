package org.nutz.walnut.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.util.NutMap;

public class WnTest {

    @Test
    public void test_wildcard() {
        assertTrue(Wn.matchWildcard("abc", "abc*"));
        assertTrue(Wn.matchWildcard("abcD", "abc*"));
        assertTrue(Wn.matchWildcard("abc", "*abc"));
        assertTrue(Wn.matchWildcard("Xabc", "*abc"));
        assertTrue(Wn.matchWildcard("abc", "*abc*"));
        assertTrue(Wn.matchWildcard("XabcY", "*abc*"));
        assertTrue(Wn.matchWildcard("Xabc", "*abc*"));
        assertTrue(Wn.matchWildcard("abcY", "*abc*"));
        assertTrue(Wn.matchWildcard("abcY", "**"));
        assertTrue(Wn.matchWildcard("abcY", "*"));

        assertFalse(Wn.matchWildcard("xyz", "xz*"));
        assertFalse(Wn.matchWildcard("xyz", "*t*"));
    }

    @Test
    public void test_normalize() {
        NutMap env = new NutMap();
        env.setv("HOME", "/home/zozoh");
        env.setv("PWD", "$HOME/workspace/test");
        env.setv("ABC", "haha");

        assertEquals("/home/zozoh/bin", Wn.normalizePath("~/bin", env));
        assertEquals("/home/zozoh/workspace/test/bin", Wn.normalizePath("./bin", env));
        assertEquals("cmd_echo 'haha'", Wn.normalizeStr("cmd_echo '$ABC'", env));
        assertEquals("~/abc", Wn.normalizeStr("~/abc", env));
    }

}
