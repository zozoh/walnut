package org.nutz.walnut.util.tmpl;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Wlang;

public class WnTmplXTest {

    @Test
    public void test_loop_in_loop() {
        NutMap vars = new NutMap();
        NutMap wang = Wlang.map("name", "wang");
        wang.put("pets", Json.fromJson("[{name:'A'},{name:'B'}]"));
        NutMap zhang = Wlang.map("name", "zhang");
        zhang.put("pets", Json.fromJson("[{name:'C'},{name:'D'}]"));
        vars.put("users", Wlang.list(wang, zhang));

        String s = "SEE ${=users.size} Users: ${#loop u:users}"
                   + "${u.name}("
                   + "${#loop pet:u.pets}"
                   + "【${pet.name}】"
                   + "${#end}"
                   + ")"
                   + "${#end}";
        WnTmplX tmpl = WnTmplX.parse(s);

        String str = tmpl.render(vars);
        assertEquals("SEE 2 Users: wang(【A】【B】)zhang(【C】【D】)", str);
    }

    @Test
    public void test_branch_in_loop() {
        NutMap vars = new NutMap();
        vars.put("pets", Json.fromJson("[{name:'cat'},{name:'dog'}]"));
        vars.put("foods", Json.fromJson("[{name:'cake'},{name:'banana'}]"));

        String input = "${#loop pet,i=1 : pets}"
                       + "${#if 'pet.name' : 'cat' }"
                       + "${i}-CAT: ${pet.name}"
                       + "${#else-if 'pet.name' : 'dog' }"
                       + "${i}-DOG: ${pet.name}"
                       + "${#else}"
                       + "${i} - Unkown: ${pet.name}"
                       + "${#end}"
                       + " > "
                       + "${#end}";
        WnTmplX tmpl = WnTmplX.parse(input);

        String str = tmpl.render(vars);
        assertEquals("1-CAT: cat > 2-DOG: dog > ", str);

    }

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
    public void test_branch_if_note() {
        NutMap vars = Wlang.map("{a:100,b:20}");

        String str = WnTmplX.exec("${#if not a:'[0,99]'}A=${a}${#else}B=${b}${#end}", vars);
        assertEquals("A=100", str);

        str = WnTmplX.exec("${#if not a:'[0,100]'}A=${a}${#else}B=${b}${#end}", vars);
        assertEquals("B=20", str);
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

    @Test
    public void test_uppercase() {
        NutMap context = Lang.map("s", "abc");
        assertEquals("ABC", WnTmplX.exec("${s<string:upper>}", context));
    }

    @Test
    public void test_getOr() {
        NutMap context = Lang.map("a", "AAA");
        assertEquals("AAA", WnTmplX.exec("${b|a?-nil-}", context));
    }

    @Test
    public void test_dft_true_false() {
        NutMap context = Lang.map("a", true);
        assertEquals("x", WnTmplX.exec("${a<boolean:x>}", context));
        assertEquals("", WnTmplX.exec("${a<boolean:x/>}", context));
        assertEquals("y", WnTmplX.exec("${a<boolean:x/y>}", context));
        assertEquals("x", WnTmplX.exec("${a<boolean:/x>}", context));
    }

    @Test
    public void test_string_replace() {
        NutMap context = Lang.map("path:'  ~/a/b/c  '");
        assertEquals("-a-b-c",
                     WnTmplX.exec("${path<:@trim;@replace'/','-';@replace'~'>}", context, true));
    }

    @Test
    public void test_string_mapping() {
        NutMap context = Lang.map("fruit:'A'");
        assertEquals("Apple", WnTmplX.exec("${fruit(::A=Apple,B=Banana,C=Cherry)}", context, true));
        assertEquals("Apple", WnTmplX.exec("${fruit<::A=Apple,B=Banana,C=Cherry>}", context, true));
    }

    @Test
    public void test_bracket_mode() {
        assertEquals("A100C", WnTmplX.exec("A${b(int)?89}C", Lang.map("b:100")));
        assertEquals("A89C", WnTmplX.exec("A${b(int)?89}C", null));
    }

    @Test
    public void test_json_format() {
        assertEquals("null", WnTmplX.exec("${a<json>}", Lang.map("")));
        assertEquals("null", WnTmplX.exec("${a<json>}", Lang.map("a:null")));
        assertEquals("{x:100,y:99}", WnTmplX.exec("${a<json:c>}", Lang.map("a:{x:100,y:99}")));
        assertEquals("{\"x\":100,\"y\":99}",
                     WnTmplX.exec("${a<json:cq>}", Lang.map("a:{x:100,y:99}")));
        assertEquals("\"\"", WnTmplX.exec("${a<json>?}", Lang.map("")));
        assertEquals("[]", WnTmplX.exec("${a<json>?[]}", Lang.map("")));
        assertEquals("{}", WnTmplX.exec("${a<json>?-obj-}", Lang.map("")));
        assertEquals("\"xyz\"", WnTmplX.exec("${a<json>?-obj-}", Lang.map("a:'xyz'")));
        assertEquals("{k:[3, true, \"a\"]}",
                     WnTmplX.exec("${a<json:c>?-obj-}", Lang.map("a:{k:[3,true,'a']}")));
    }

    @Test
    public void test_string_format() {
        assertEquals("AB   C", WnTmplX.exec("A${b<:%-4s>?}C", Lang.map("b:'B'}")));
        // assertEquals("AB C", Tmpl.exec("A${b<string:%-4s>?}C",
        // Lang.map("b:'B'}")));
    }

    @Test
    public void test_escape() {
        assertEquals("A${b}C", WnTmplX.exec("A$${b}C", Lang.map("b:'BB'}")));
        assertEquals("${A}", WnTmplX.exec("$${${x}}", Lang.map("x:'A'")));
    }

    @Test
    public void test_dynamic_dft() {
        assertEquals("ABC", WnTmplX.exec("A${b?@x}C", Lang.map("x:'B'}")));
    }

    @Test
    public void test_empty_dft() {
        assertEquals("AC", WnTmplX.exec("A${b?}C", Lang.map("x:'B'}")));
        assertEquals("ABC", WnTmplX.exec("A${b?}C", Lang.map("b:'B'}")));
    }

    @Test
    public void test_special_key() {
        assertEquals("ABC", WnTmplX.exec("A${a-b}C", Lang.map("'a-b':'B'}")));
        assertEquals("ABC", WnTmplX.exec("A${'a.b'}C", Lang.map("'a.b':'B'}")));
        assertEquals("A1C",
                     WnTmplX.exec("A${pos[0].'x.x'}C", Lang.map("pos:[{'x.x':1},{'y.y':2}]}")));
        assertEquals("A2C",
                     WnTmplX.exec("A${pos[1].'y.y'}C", Lang.map("pos:[{'x.x':1},{'y.y':2}]}")));
    }

    @Test
    public void test_string() {
        assertEquals("ABC", WnTmplX.exec("A${a.b}C", Lang.map("a:{b:'B'}")));
        assertEquals("ABC", WnTmplX.exec("A${a.b[1]}C", Lang.map("a:{b:['A','B','C']}")));
        assertEquals("ABC", WnTmplX.exec("A${a?B}C", null));
    }

    @Test
    public void test_int() {
        assertEquals("003", WnTmplX.exec("${n<int:%03d>}", Lang.map("n:3")));
        assertEquals("010", WnTmplX.exec("${n<int:%03X>}", Lang.map("n:16")));
    }

    @Test
    public void test_simple_float() {
        assertEquals("3.00", WnTmplX.exec("${n<float>}", Lang.map("n:3")));
        assertEquals("0.98", WnTmplX.exec("${n<float>?.984}", null));
    }

    @Test
    public void test_date() {
        long ms = System.currentTimeMillis();
        Date d = Times.D(ms);
        String sd = Times.format("yyyy-MM-dd'T'HH:mm:ss", d);
        assertEquals(sd, WnTmplX.exec("${d<date>}", Lang.mapf("d:%s", ms)));
        assertEquals(Times.sD(d), WnTmplX.exec("${d<date:yyyy-MM-dd>}", Lang.mapf("d:'%s'", sd)));
        assertEquals("", WnTmplX.exec("${xyz<date:yyyy-MM-dd>?}", new NutMap()));
    }

    @Test
    public void test_boolean() {
        assertEquals("yes", WnTmplX.exec("${v<boolean:no/yes>}", Lang.map("v:true")));
        assertEquals("no", WnTmplX.exec("${v<boolean:no/yes>}", Lang.map("v:false")));
        assertEquals("no", WnTmplX.exec("${v<boolean:no/yes>?false}", null));

        assertEquals("是", WnTmplX.exec("${v<boolean:否/是>}", Lang.map("v:true")));
        assertEquals("否", WnTmplX.exec("${v<boolean:否/是>}", Lang.map("v:false")));
        assertEquals("否", WnTmplX.exec("${v<boolean:否/是>?false}", null));

        assertEquals("false", WnTmplX.exec("${v<boolean>?false}", null));
        assertEquals("true", WnTmplX.exec("${v<boolean>?true}", Lang.map("{}")));

        assertEquals("false", WnTmplX.exec("${v<boolean>}", null));
        assertEquals("false", WnTmplX.exec("${v<boolean>}", Lang.map("{}")));

        assertEquals("yes", WnTmplX.exec("${v<boolean:/yes>}", Lang.map("v:true")));
        assertEquals("", WnTmplX.exec("${v<boolean:/yes>}", Lang.map("v:false")));
    }
}