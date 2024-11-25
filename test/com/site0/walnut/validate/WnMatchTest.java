package com.site0.walnut.validate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

public class WnMatchTest {

    @Test
    public void test_str_float_range() {
        AutoMatch m = new AutoMatch("(0.0,)");
        assertTrue(m.match("23.689"));

        AutoMatch m2 = new AutoMatch("(0,)");
        assertTrue(m2.match("23.689"));
    }

    @Test
    public void test_exist2() {
        NutMap map = Wlang.map("{a: {x:1,y:2},b:{x:3,y:4}}");
        AutoMatch m = new AutoMatch(Wlang.map("'b.y':'[EXISTS]'"));
        assertTrue(m.match(map));
        m = new AutoMatch(Wlang.map("'b.y':'![EXISTS]'"));
        assertFalse(m.match(map));
    }

    @Test
    public void test_findInArray() {
        NutMap map = Wlang.map("{'matchMode':'findInArray','matchBy':{'type':'W'}}");
        AutoMatch m = new AutoMatch(map);
        List<NutMap> list = new ArrayList<>(3);
        list.add(Wlang.map("type:'X',age:24"));
        list.add(Wlang.map("type:'Y',age:25"));
        assertFalse(m.match(list));

        list.add(Wlang.map("type:'W',age:26"));
        assertTrue(m.match(list));
    }

    @Test
    public void test_blank() {
        AutoMatch m = new AutoMatch(Wlang.map("xyz", "[BLANK]"));
        NutMap input = Wlang.map("xyz", null);
        assertTrue(m.match(input));

        input = Wlang.map("xyz", "haha");
        assertFalse(m.match(input));

        m = new AutoMatch(Wlang.map("xyz", "![BLANK]"));
        input = Wlang.map("xyz", null);
        assertFalse(m.match(input));

        input = Wlang.map("xyz", "haha");
        assertTrue(m.match(input));
    }

    @Test
    public void test_match_exists2() {
        AutoMatch m = new AutoMatch(Wlang.map("a.b.c", "[EXISTS]"));
        NutMap input = Wlang.map("xyz", null);
        assertFalse(m.match(input));

        input = Wlang.map("{a:{b:{c:null}}}");
        assertTrue(m.match(input));

        input = Wlang.map("{a:{b:{c:true}}}");
        assertTrue(m.match(input));
    }

    @Test
    public void test_match_exists() {
        AutoMatch m = new AutoMatch(Wlang.map("abc", "[EXISTS]"));
        NutMap input = Wlang.map("xyz", null);
        assertFalse(m.match(input));

        input = Wlang.map("abc", null);
        assertTrue(m.match(input));

        input = Wlang.map("abc", true);
        assertTrue(m.match(input));

        m = new AutoMatch(Wlang.map("!abc", "[EXISTS]"));

        input = Wlang.map("xyz", null);
        assertTrue(m.match(input));

        input = Wlang.map("abc", null);
        assertFalse(m.match(input));

        input = Wlang.map("abc", true);
        assertFalse(m.match(input));

        m = new AutoMatch(Wlang.map("abc", "![EXISTS]"));

        input = Wlang.map("xyz", null);
        assertTrue(m.match(input));

        input = Wlang.map("abc", null);
        assertFalse(m.match(input));

        input = Wlang.map("abc", true);
        assertFalse(m.match(input));
    }

    @Test
    public void test_match_str3() {
        String regex = "^image/";
        AutoMatch m = new AutoMatch(regex);
        boolean re = m.match("image/jpeg");
        assertTrue(re);
    }

    @Test
    public void test_match_str2() {
        String regex = "!^/home/.+/(.thumbnail/gen|.publish/gen|www)";
        AutoMatch m = new AutoMatch(regex);
        boolean re = m.match("/home/demo/site/path/to/a.jpg");
        assertTrue(re);
    }

    @Test
    public void test_match_str() {
        NutMap obj, map;
        WnMatch vli;

        obj = Wlang.map("name:'xiaobai', age:12");

        map = Wlang.map("name:'xiaobai', age:'[10, 15]'");
        vli = new AutoMatch(map);
        assertTrue(vli.match(obj));

        map = Wlang.map("name:'xiaobai', age:'[13, 15]'");
        vli = new AutoMatch(map);
        assertFalse(vli.match(obj));

        map = Wlang.map("name:'xiaobai', age:'(10, 15]'");
        vli = new AutoMatch(map);
        assertTrue(vli.match(obj));

        map = Wlang.map("age:'(12, 15)'");
        vli = new AutoMatch(map);
        assertFalse(vli.match(obj));

        map = Wlang.map("name:'^xiao.+$'");
        vli = new AutoMatch(map);
        assertTrue(vli.match(obj));

        map = Wlang.map("!name:'^xiao.+$'");
        vli = new AutoMatch(map);
        assertFalse(vli.match(obj));

        map = Wlang.map("name:'^y.+$'");
        vli = new AutoMatch(map);
        assertFalse(vli.match(obj));
    }

    @Test
    public void test_simple() {
        NutMap obj, map;
        WnMatch vli;

        obj = Wlang.map("x:100, y:99");

        map = Wlang.map("x:100, y:99");
        vli = new AutoMatch(map);
        assertTrue(vli.match(obj));

        map = Wlang.map("x:100");
        vli = new AutoMatch(map);
        assertTrue(vli.match(obj));

        map = Wlang.map("y:99");
        vli = new AutoMatch(map);
        assertTrue(vli.match(obj));

        map = Wlang.map("y:98");
        vli = new AutoMatch(map);
        assertFalse(vli.match(obj));

        map = Wlang.map("z:'notNil'");
        vli = new AutoMatch(map);
        assertFalse(vli.match(obj));
    }

}
