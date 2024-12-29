package com.site0.walnut.util.tmpl;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;

public class WnTmplTest {
    
    @Test
    public void test_uppercase() {
        NutMap context = Wlang.map("s", "abc");
        assertEquals("ABC", WnTmpl.exec("${s<string:upper>}", context));
    }

    @Test
    public void test_getOr() {
        NutMap context = Wlang.map("a", "AAA");
        assertEquals("AAA", WnTmpl.exec("${b|a?-nil-}", context));
    }

    @Test
    public void test_dft_true_false() {
        NutMap context = Wlang.map("a", true);
        assertEquals("x", WnTmpl.exec("${a<boolean:x>}", context));
        assertEquals("", WnTmpl.exec("${a<boolean:x/>}", context));
        assertEquals("y", WnTmpl.exec("${a<boolean:x/y>}", context));
        assertEquals("x", WnTmpl.exec("${a<boolean:/x>}", context));
    }

    @Test
    public void test_string_replace() {
        NutMap context = Wlang.map("path:'  ~/a/b/c  '");
        assertEquals("-a-b-c",
                     WnTmpl.exec("${path<:@trim;@replace'/','-';@replace'~'>}", context, true));
    }

    @Test
    public void test_string_mapping() {
        NutMap context = Wlang.map("fruit:'A'");
        assertEquals("Apple", WnTmpl.exec("${fruit(::A=Apple,B=Banana,C=Cherry)}", context, true));
        assertEquals("Apple", WnTmpl.exec("${fruit<::A=Apple,B=Banana,C=Cherry>}", context, true));
    }

    @Test
    public void test_customized_a() {
        assertEquals("A100C", WnTmpl.exec("A@<b(int)?89>C", "@", "<", ">", Wlang.map("b:100"), true));
        assertEquals("A100C", WnTmpl.exec("A@{b(int)?89}C", "@", Wlang.map("b:100"), true));
    }

    @Test
    public void test_bracket_mode() {
        assertEquals("A100C", WnTmpl.exec("A${b(int)?89}C", Wlang.map("b:100")));
        assertEquals("A89C", WnTmpl.exec("A${b(int)?89}C", null));
    }

    @Test
    public void test_json_format() {
        assertEquals("null", WnTmpl.exec("${a<json>}", Wlang.map("")));
        assertEquals("null", WnTmpl.exec("${a<json>}", Wlang.map("a:null")));
        assertEquals("{x:100,y:99}", WnTmpl.exec("${a<json:c>}", Wlang.map("a:{x:100,y:99}")));
        assertEquals("{\"x\":100,\"y\":99}",
                     WnTmpl.exec("${a<json:cq>}", Wlang.map("a:{x:100,y:99}")));
        assertEquals("\"\"", WnTmpl.exec("${a<json>?}", Wlang.map("")));
        assertEquals("[]", WnTmpl.exec("${a<json>?[]}", Wlang.map("")));
        assertEquals("{}", WnTmpl.exec("${a<json>?-obj-}", Wlang.map("")));
        assertEquals("\"xyz\"", WnTmpl.exec("${a<json>?-obj-}", Wlang.map("a:'xyz'")));
        assertEquals("{k:[3, true, \"a\"]}",
                     WnTmpl.exec("${a<json:c>?-obj-}", Wlang.map("a:{k:[3,true,'a']}")));
    }

    @Test
    public void test_string_format() {
        assertEquals("AB   C", WnTmpl.exec("A${b<:%-4s>?}C", Wlang.map("b:'B'}")));
        // assertEquals("AB C", Tmpl.exec("A${b<string:%-4s>?}C",
        // Wlang.map("b:'B'}")));
    }

    @Test
    public void test_escape() {
        assertEquals("A${b}C", WnTmpl.exec("A$${b}C", Wlang.map("b:'BB'}")));
        assertEquals("${A}", WnTmpl.exec("$${${x}}", Wlang.map("x:'A'")));
    }

    @Test
    public void test_dynamic_dft() {
        assertEquals("ABC", WnTmpl.exec("A${b?@x}C", Wlang.map("x:'B'}")));
    }

    @Test
    public void test_empty_dft() {
        assertEquals("AC", WnTmpl.exec("A${b?}C", Wlang.map("x:'B'}")));
        assertEquals("ABC", WnTmpl.exec("A${b?}C", Wlang.map("b:'B'}")));
    }

    @Test
    public void test_special_key() {
        assertEquals("ABC", WnTmpl.exec("A${a-b}C", Wlang.map("'a-b':'B'}")));
        assertEquals("ABC", WnTmpl.exec("A${'a.b'}C", Wlang.map("'a.b':'B'}")));
        assertEquals("A1C", WnTmpl.exec("A${pos[0].'x.x'}C", Wlang.map("pos:[{'x.x':1},{'y.y':2}]}")));
        assertEquals("A2C", WnTmpl.exec("A${pos[1].'y.y'}C", Wlang.map("pos:[{'x.x':1},{'y.y':2}]}")));
    }

    @Test
    public void test_string() {
        assertEquals("ABC", WnTmpl.exec("A${a.b}C", Wlang.map("a:{b:'B'}")));
        assertEquals("ABC", WnTmpl.exec("A${a.b[1]}C", Wlang.map("a:{b:['A','B','C']}")));
        assertEquals("ABC", WnTmpl.exec("A${a?B}C", null));
    }

    @Test
    public void test_int() {
        assertEquals("003", WnTmpl.exec("${n<int:%03d>}", Wlang.map("n:3")));
        assertEquals("010", WnTmpl.exec("${n<int:%03X>}", Wlang.map("n:16")));
    }

    @Test
    public void test_simple_float() {
        assertEquals("3", WnTmpl.exec("${n<float>}", Wlang.map("n:3")));
        assertEquals("0.98", WnTmpl.exec("${n<float>?.984}", null));
    }

    @Test
    public void test_date() {
        long ms = System.currentTimeMillis();
        Date d = Times.D(ms);
        String sd = Times.format("yyyy-MM-dd'T'HH:mm:ss", d);
        assertEquals(sd, WnTmpl.exec("${d<date>}", Wlang.mapf("d:%s", ms)));
        assertEquals(Times.sD(d), WnTmpl.exec("${d<date:yyyy-MM-dd>}", Wlang.mapf("d:'%s'", sd)));
        assertEquals("", WnTmpl.exec("${xyz<date:yyyy-MM-dd>?}", new NutMap()));
    }

    @Test
    public void test_boolean() {
        assertEquals("yes", WnTmpl.exec("${v<boolean:no/yes>}", Wlang.map("v:true")));
        assertEquals("no", WnTmpl.exec("${v<boolean:no/yes>}", Wlang.map("v:false")));
        assertEquals("no", WnTmpl.exec("${v<boolean:no/yes>?false}", null));

        assertEquals("是", WnTmpl.exec("${v<boolean:否/是>}", Wlang.map("v:true")));
        assertEquals("否", WnTmpl.exec("${v<boolean:否/是>}", Wlang.map("v:false")));
        assertEquals("否", WnTmpl.exec("${v<boolean:否/是>?false}", null));

        assertEquals("false", WnTmpl.exec("${v<boolean>?false}", null));
        assertEquals("true", WnTmpl.exec("${v<boolean>?true}", Wlang.map("{}")));

        assertEquals("false", WnTmpl.exec("${v<boolean>}", null));
        assertEquals("false", WnTmpl.exec("${v<boolean>}", Wlang.map("{}")));

        assertEquals("yes", WnTmpl.exec("${v<boolean:/yes>}", Wlang.map("v:true")));
        assertEquals("", WnTmpl.exec("${v<boolean:/yes>}", Wlang.map("v:false")));
    }

}
