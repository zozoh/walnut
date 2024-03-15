package org.nutz.lang.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.util.Wlang;

public class NutMapTest {

    @Test
    public void test_match_0() {
        assertTrue(Wlang.map("a:'23',x:100").match(Wlang.map("a:'23',x:100,y:48")));
        assertTrue(Wlang.map("a:'23',x:100").match(Wlang.map("a:['a','23','b'],x:100,y:48")));
        assertTrue(Wlang.map("{}").match(Wlang.map("a:['a','23','b'],x:100,y:48")));

        assertFalse(Wlang.map("a:'20',x:100").match(Wlang.map("a:'23',x:100,y:48")));
        assertFalse(Wlang.map("a:'23',x:101").match(Wlang.map("a:['a','23','b'],x:100,y:48")));
    }

    @Test
    public void test_add_element_with_same_key() {
        NutMap nutMap = new NutMap();
        nutMap.addv("a", "a1").addv("a", "a2");

        List<?> key = (List<?>) nutMap.get("a");
        assertEquals("a1", key.get(0));
        assertEquals("a2", key.get(1));
    }

    @Test
    public void test_get_element_cast_type() {
        NutMap nutMap = new NutMap();
        nutMap.addv("a", "123").addv("b", 123);
        assertEquals(123, nutMap.getInt("a"));
        assertEquals("123", nutMap.getString("b"));
    }

    @Test
    public void test_add_string() {
        NutMap nutMap = new NutMap();
        List<String> sList = new ArrayList<String>();
        sList.add("s1");
        nutMap.setv("sList", sList);
        List<String> sList2 = nutMap.getList("sList", String.class);
        assertEquals("s1", sList2.get(0));
    }

    @SuppressWarnings("unchecked")
    @Ignore
    @Test
    public void test_add_string2() {
        NutMap nutMap = new NutMap();
        List<String> sList = new ArrayList<String>();
        nutMap.setv("sList", sList);
        List<String> sList2 = nutMap.getAs("sList", List.class);
        sList2.add("s1");
        List<String> sList3 = nutMap.getAs("sList", List.class);
        assertEquals("s1", sList3.get(0));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_add_nutmap() {
        NutMap nutMap = new NutMap();
        List<NutMap> sList = new ArrayList<NutMap>();
        nutMap.setv("sList", sList);
        // 获取list，添加内容
        List<NutMap> sList2 = (List<NutMap>) nutMap.get("sList");
        sList2.add(new NutMap().setv("nm", "s1"));
        // 再获取看看
        List<NutMap> sList3 = (List<NutMap>) nutMap.get("sList");
        assertEquals("s1", sList3.get(0).getString("nm"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_add_nutmap2() {
        NutMap nutMap = new NutMap();
        List<NutMap> sList = new ArrayList<NutMap>();
        nutMap.setv("sList", sList);
        // 获取list，添加内容
        List<NutMap> sList2 = nutMap.getAs("sList", List.class);
        sList2.add(new NutMap().setv("nm", "s1"));
        // 再获取看看
        List<NutMap> sList3 = nutMap.getAs("sList", List.class);
        assertEquals("s1", sList3.get(0).getString("nm"));
    }

    @Test
    public void test_attach() {
        NutMap nutMap = new NutMap();
        nutMap.setv( "key", "nutMap1");
        NutMap nutMap2 = new NutMap();
        nutMap2.setv( "key", "nutMap2");
        nutMap.attach( nutMap2);

        assertEquals( "nutMap1", nutMap.get("key"));
        assertEquals( "{\"key\":\"nutMap1\"}", Json.toJson( nutMap, JsonFormat.compact()));
    }

}
