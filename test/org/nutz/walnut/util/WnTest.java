package org.nutz.walnut.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.usr.IoWnSession;
import org.nutz.walnut.impl.usr.IoWnUsr;

public class WnTest {

    @Test
    public void test_parse_mode() {
        assertEquals("rwxrwxrwx", Wn.Io.modeToStr(0777));
        assertEquals("rwxr-xr--", Wn.Io.modeToStr(0754));
        assertEquals("rwxr-xr-x", Wn.Io.modeToStr(0755));
        assertEquals("rwx------", Wn.Io.modeToStr(0700));
        assertEquals("r-x------", Wn.Io.modeToStr(0500));
        assertEquals("---------", Wn.Io.modeToStr(0000));

        assertEquals(0777, Wn.Io.modeFromStr("rwxrwxrwx"));
        assertEquals(0754, Wn.Io.modeFromStr("rwxr-xr--"));
        assertEquals(0755, Wn.Io.modeFromStr("rwxr-xr-x"));
        assertEquals(0700, Wn.Io.modeFromStr("rwx------"));
        assertEquals(0500, Wn.Io.modeFromStr("r-x------"));
        assertEquals(0000, Wn.Io.modeFromStr("---------"));

    }

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
