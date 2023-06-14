package org.nutz.walnut.util.tmpl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Wlang;

public class WnTmplXTest {

    @Test
    public void test_simple() {
        NutMap vars = Wlang.map("{a:100,b:20}");

        String str = WnTmplX.exec("a=${a}", vars);
        assertEquals("a=100", str);
    }

    @Test
    public void test_simple_if() {
        NutMap vars = Wlang.map("{a:100,b:20}");

        String str = WnTmplX.exec("${#if a:'[0,99]'}A=${a}${#else}B=${b}${#end}", vars);
        assertEquals("B=20", str);

        vars.put("a", 80);
        str = WnTmplX.exec("${#if a:'[0,99]'}A=${a}${#else}B=${b}${#end}", vars);
        assertEquals("A=80", str);
    }

}
