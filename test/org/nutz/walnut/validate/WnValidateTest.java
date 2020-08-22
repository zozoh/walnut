package org.nutz.walnut.validate;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;

public class WnValidateTest {

    @Test
    public void test_match_str() {
        NutMap obj, map;
        WnValidate vli;

        obj = Lang.map("name:'xiaobai', age:12");

        map = Lang.map("name:'=xiaobai', age:['inRange', 10, 15]");
        vli = new WnValidate(map);
        assertTrue(vli.match(obj));

        map = Lang.map("name:'=xiaobai', age:['inRange', 13, 15]");
        vli = new WnValidate(map);
        assertFalse(vli.match(obj));

        map = Lang.map("name:'=xiaobai', age:'(10, 15)'");
        vli = new WnValidate(map);
        assertTrue(vli.match(obj));

        map = Lang.map("age:'(12, 15)'");
        vli = new WnValidate(map);
        assertFalse(vli.match(obj));

        map = Lang.map("name:'^xiao.+$'");
        vli = new WnValidate(map);
        assertTrue(vli.match(obj));

        map = Lang.map("!name:'^xiao.+$'");
        vli = new WnValidate(map);
        assertFalse(vli.match(obj));

        map = Lang.map("name:'^y.+$'");
        vli = new WnValidate(map);
        assertFalse(vli.match(obj));
    }

    @Test
    public void test_simple() {
        NutMap obj, map;
        WnValidate vli;

        obj = Lang.map("x:100, y:99");

        map = Lang.map("x:100, y:99");
        vli = new WnValidate(map);
        assertTrue(vli.match(obj));

        map = Lang.map("x:100");
        vli = new WnValidate(map);
        assertTrue(vli.match(obj));

        map = Lang.map("x:['isEqual', 100]");
        vli = new WnValidate(map);
        assertTrue(vli.match(obj));

        map = Lang.map("y:99");
        vli = new WnValidate(map);
        assertTrue(vli.match(obj));

        map = Lang.map("y:['isEqual', 99]");
        vli = new WnValidate(map);
        assertTrue(vli.match(obj));

        map = Lang.map("y:98");
        vli = new WnValidate(map);
        assertFalse(vli.match(obj));

        map = Lang.map("y:{name:'isEqual', args:[98]}");
        vli = new WnValidate(map);
        assertFalse(vli.match(obj));

        map = Lang.map("z:'notNil'");
        vli = new WnValidate(map);
        assertFalse(vli.match(obj));
    }

}
