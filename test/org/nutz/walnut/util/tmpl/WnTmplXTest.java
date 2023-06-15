package org.nutz.walnut.util.tmpl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Wlang;

public class WnTmplXTest {

    @Test
    public void test_loop_in_branch() {
        NutMap vars = new NutMap();
        vars.put("pets", Json.fromJson("[{name:'cat'},{name:'dog'}]"));
        vars.put("foods", Json.fromJson("[{name:'cake'},{name:'banana'}]"));

        String input = "${#if a:'[0,100)'}"
                      + "${#loop pet,i=1 : pets}${i}.${pet.name};${#end}"
                      + "${#else-if a:'[100,200)'}"
                      + "${#loop fo,i=1 : foods}${i}.${fo.name};${#end}"
                      + "${#else}"
                      + "NoNo"
                      + "${#end}";
        WnTmplX tmpl = WnTmplX.parse(input);

        vars.put("a", 99);
        String str = tmpl.render(vars);
        assertEquals("1.cat;2.dog;", str);

        vars.put("a", 100);
        str = tmpl.render(vars);
        assertEquals("1.cake;2.banana;", str);

        vars.put("a", 300);
        str = tmpl.render(vars);
        assertEquals("NoNo", str);
    }

    @Test
    public void test_loop() {
        NutMap vars = Wlang.map("{a:100,b:20}");
        vars.put("pets", Json.fromJson("[{name:'xiaobai'},{name:'xiaohei'}]"));

        String tmpl = "${#loop pet,i=1 : pets}${i}.${pet.name};${#end}";
        String str = WnTmplX.exec(tmpl, vars);
        assertEquals("1.xiaobai;2.xiaohei;", str);
    }

    @Test
    public void test_branch_if() {
        NutMap vars = Wlang.map("{a:100,b:20}");

        String str = WnTmplX.exec("${#if a:'[0,99]'}A=${a}${#else}B=${b}${#end}", vars);
        assertEquals("B=20", str);

        vars.put("a", 80);
        str = WnTmplX.exec("${#if a:'[0,99]'}A=${a}${#else}B=${b}${#end}", vars);
        assertEquals("A=80", str);
    }

    @Test
    public void test_simple() {
        NutMap vars = Wlang.map("{a:100,b:20}");

        String str = WnTmplX.exec("a=${a}", vars);
        assertEquals("a=100", str);
    }
}
