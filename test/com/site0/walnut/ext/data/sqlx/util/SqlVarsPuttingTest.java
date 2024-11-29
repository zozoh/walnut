package com.site0.walnut.ext.data.sqlx.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.Wlang;
import static com.site0.walnut.ext.data.sqlx.util.SqlVarsPutting.*;

public class SqlVarsPuttingTest {

    @Test
    public void test_dynamic_key_path() {
        NutMap names = Wlang.map("{A:'red',B:'green',C:'blue'}");
        NutMap pipe = Wlang.map("color", names);

        NutMap a = Wlang.map("{id:'A'}");
        exec("color=color.${id}", a, pipe);
        assertEquals("red", a.get("color"));

        NutMap b = Wlang.map("{id:'B'}");
        exec("color=color.${id}", b, pipe);
        assertEquals("green", b.get("color"));

        NutMap c = Wlang.map("{id:'C'}");
        exec("color=color.${id}", c, pipe);
        assertEquals("blue", c.get("color"));
    }

    @Test
    public void test_key_path() {
        NutMap pipe = Wlang.map("{color: {name:'red'}}");
        NutMap bean = Wlang.map("{}");

        exec("my.color=color.name", bean, pipe);
        assertEquals("red", bean.getAs("my", NutMap.class).get("color"));
    }

    @Test
    public void test_simple() {
        NutMap pipe = Wlang.map("{name:'red'}");
        NutMap bean = Wlang.map("{}");

        exec("name=name", bean, pipe);
        assertEquals("red", bean.get("name"));
    }

}
