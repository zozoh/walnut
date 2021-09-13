package org.nutz.walnut.validate;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class WnMatchTest {

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

        obj = Lang.map("name:'xiaobai', age:12");

        map = Lang.map("name:'xiaobai', age:'[10, 15]'");
        vli = new AutoMatch(map);
        assertTrue(vli.match(obj));

        map = Lang.map("name:'xiaobai', age:'[13, 15]'");
        vli = new AutoMatch(map);
        assertFalse(vli.match(obj));

        map = Lang.map("name:'xiaobai', age:'(10, 15]'");
        vli = new AutoMatch(map);
        assertTrue(vli.match(obj));

        map = Lang.map("age:'(12, 15)'");
        vli = new AutoMatch(map);
        assertFalse(vli.match(obj));

        map = Lang.map("name:'^xiao.+$'");
        vli = new AutoMatch(map);
        assertTrue(vli.match(obj));

        map = Lang.map("!name:'^xiao.+$'");
        vli = new AutoMatch(map);
        assertFalse(vli.match(obj));

        map = Lang.map("name:'^y.+$'");
        vli = new AutoMatch(map);
        assertFalse(vli.match(obj));
    }

    @Test
    public void test_simple() {
        NutMap obj, map;
        WnMatch vli;

        obj = Lang.map("x:100, y:99");

        map = Lang.map("x:100, y:99");
        vli = new AutoMatch(map);
        assertTrue(vli.match(obj));

        map = Lang.map("x:100");
        vli = new AutoMatch(map);
        assertTrue(vli.match(obj));

        map = Lang.map("x:100");
        vli = new AutoMatch(map);
        assertTrue(vli.match(obj));

        map = Lang.map("y:99");
        vli = new AutoMatch(map);
        assertTrue(vli.match(obj));

        map = Lang.map("y:99");
        vli = new AutoMatch(map);
        assertTrue(vli.match(obj));

        map = Lang.map("y:98");
        vli = new AutoMatch(map);
        assertFalse(vli.match(obj));

        map = Lang.map("y:98");
        vli = new AutoMatch(map);
        assertFalse(vli.match(obj));

        map = Lang.map("z:'notNil'");
        vli = new AutoMatch(map);
        assertFalse(vli.match(obj));
    }

}
