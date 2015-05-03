package org.nutz.walnut.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.usr.IoWnSession;
import org.nutz.walnut.impl.usr.IoWnUsr;

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
        WnSystem sys = new WnSystem();
        sys.se = new IoWnSession();
        sys.se.env("HOME", "/home/zozoh");
        sys.se.env("PWD", "$HOME/workspace/test");
        sys.se.env("ABC", "haha");
        sys.me = new IoWnUsr();
        sys.me.home("/home/zozoh");

        assertEquals("/home/zozoh/bin", Wn.normalizePath("~/bin", sys));
        assertEquals("/home/zozoh/workspace/test/bin", Wn.normalizePath("./bin", sys));
        assertEquals("cmd_echo 'haha'", Wn.normalizeStr("cmd_echo '$ABC'", sys.se.envs()));
        assertEquals("~/abc", Wn.normalizeStr("~/abc", sys.se.envs()));
    }

}
