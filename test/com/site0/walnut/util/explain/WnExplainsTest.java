package com.site0.walnut.util.explain;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

public class WnExplainsTest {

    private NutMap _JO(String json) {
        return Json.fromJson(NutMap.class, json);
    }

    private List<Object> _JL(String json) {
        return Json.fromJsonAsList(Object.class, json);
    }

    @Test
    public void testBaseExplain() {
        NutBean context = _JO("{name:'xiaobai',age:12}");
        NutMap src = _JO("{a:'=name',b:'=age',c:'=color?red'}");
        WnExplain wx = WnExplains.parse(src);
        NutMap out = (NutMap) wx.explain(context);
        assertEquals("xiaobai", out.get("a"));
        assertEquals(12, out.get("b"));
        assertEquals("red", out.get("c"));
    }

    @Test
    public void testValInMap() {
        NutBean context = _JO("{name:'xiaobai',age:12}");
        NutMap src = _JO("{it:{a:'=name',b:'=age',c:'=color?red'}}");
        WnExplain wx = WnExplains.parse(src);
        NutMap out = (NutMap) wx.explain(context);
        NutMap it = out.getAs("it", NutMap.class);
        assertEquals("xiaobai", it.get("a"));
        assertEquals(12, it.get("b"));
        assertEquals("red", it.get("c"));
    }

    @Test
    public void testCutString() {
        NutBean context = _JO("{name:'xiao bai'}");

        // Test sub=4
        NutMap src1 = _JO("{a:'->${name<:@sub=4>}'}");
        WnExplain wx1 = WnExplains.parse(src1);
        NutMap out1 = (NutMap) wx1.explain(context);
        assertEquals("xiao", out1.get("a"));

        // Test sub=1/3
        NutMap src2 = _JO("{a:'->${name<:@sub=1/3>}'}");
        WnExplain wx2 = WnExplains.parse(src2);
        NutMap out2 = (NutMap) wx2.explain(context);
        assertEquals("ia", out2.get("a"));

        // Test sub=5/
        NutMap src3 = _JO("{a:'->${name<:@sub=5/>}'}");
        WnExplain wx3 = WnExplains.parse(src3);
        NutMap out3 = (NutMap) wx3.explain(context);
        assertEquals("bai", out3.get("a"));

        // Test sub=/2
        NutMap src4 = _JO("{a:'->${name<:@sub=/2>}'}");
        WnExplain wx4 = WnExplains.parse(src4);
        NutMap out4 = (NutMap) wx4.explain(context);
        assertEquals("xi", out4.get("a"));
    }

    @Test
    public void testBoolExplain() {
        NutBean context = _JO("{name:'asdfasdf',age:12}");

        WnExplain wx = WnExplains.parse("==name");
        Boolean out = (Boolean) wx.explain(context);
        assertTrue(out);

        WnExplain wx2 = WnExplains.parse("!=not_exists");
        Boolean out2 = (Boolean) wx2.explain(context);
        assertTrue(out2);
    }

    @Test
    public void testMapExplain() {
        NutBean context = _JO("{name:'abc',age:12,pet:{name:'xx'}}");

        NutMap src = _JO("{a:'=name',b:'=pet.name',c:'=age'}");
        WnExplain wx = WnExplains.parse(src);
        NutMap out = (NutMap) wx.explain(context);

        assertEquals("abc", out.get("a"));
        assertEquals("xx", out.get("b"));
        assertEquals(12, out.get("c"));
    }

    @Test
    public void testArrayExplain() {
        NutBean context = _JO("{name:'abc',age:12,pet:{name:'xx'}}");

        List<Object> src = _JL("['=name', {b:'=pet.name'}, '=age']");
        WnExplain wx = WnExplains.parse(src);
        List<?> out = (List<?>) wx.explain(context);

        assertEquals("abc", out.get(0));
        assertEquals("xx", ((Map<?, ?>) out.get(1)).get("b"));
        assertEquals(12, out.get(2));
    }

    @Test
    public void testArrayScopeExplain() {
        NutBean context = _JO("{list:[{name:'abc',age:12},{name:'def',age:14},{name:'ghi',age:30}]}");

        NutMap src = _JO("{list:[{a:'=name',b:'=age'}]}");
        WnExplain wx = WnExplains.parse(src);
        NutMap out = (NutMap) wx.explain(context);

        List<?> list = (List<?>) out.get("list");
        assertEquals(3, list.size());
        assertEquals("abc", ((Map<?, ?>) list.get(0)).get("a"));
        assertEquals(12, ((Map<?, ?>) list.get(0)).get("b"));
        assertEquals("def", ((Map<?, ?>) list.get(1)).get("a"));
        assertEquals(14, ((Map<?, ?>) list.get(1)).get("b"));
        assertEquals("ghi", ((Map<?, ?>) list.get(2)).get("a"));
        assertEquals(30, ((Map<?, ?>) list.get(2)).get("b"));
    }

    @Test
    public void testArrayScopeExplain2() {
        NutBean context = Json
            .fromJson(NutMap.class,
                      "{list:[{name:'abc',age:12},{name:'def',age:14},{name:'ghi',age:30}]}");

        NutMap src = Json
            .fromJson(NutMap.class,
                      "{xyz:[':scope=list',{a:'=name',b:'=age'}]}");
        WnExplain wx = WnExplains.parse(src);
        NutMap out = (NutMap) wx.explain(context);

        List<?> xyz = (List<?>) out.get("xyz");
        assertEquals(3, xyz.size());
        assertEquals("abc", ((Map<?, ?>) xyz.get(0)).get("a"));
        assertEquals(12, ((Map<?, ?>) xyz.get(0)).get("b"));
        assertEquals("def", ((Map<?, ?>) xyz.get(1)).get("a"));
        assertEquals(14, ((Map<?, ?>) xyz.get(1)).get("b"));
        assertEquals("ghi", ((Map<?, ?>) xyz.get(2)).get("a"));
        assertEquals(30, ((Map<?, ?>) xyz.get(2)).get("b"));
    }

    @Test
    public void testArrayMapping() {
        NutBean context = _JO("{pets:[{name:'red',age:12},{name:'green',age:14},{name:'blue',age:30}]}");

        List<Object> src = _JL("[':scope=pets', {nm:'=name'}]");
        WnExplain wx = WnExplains.parse(src);
        List<?> out = (List<?>) wx.explain(context);

        assertEquals(3, out.size());
        assertEquals("red", ((Map<?, ?>) out.get(0)).get("nm"));
        assertEquals("green", ((Map<?, ?>) out.get(1)).get("nm"));
        assertEquals("blue", ((Map<?, ?>) out.get(2)).get("nm"));
    }

    @Test
    public void testWholeContextExplain() {
        NutBean context = _JO("{name:'abc',age:12,pet:{name:'xx'}}");

        WnExplain wx = WnExplains.parse("=..");
        NutMap out = (NutMap) wx.explain(context);

        assertEquals("abc", out.get("name"));
        assertEquals(12, out.get("age"));
        assertEquals("xx", ((Map<?, ?>) out.get("pet")).get("name"));
    }

    @Test
    public void testZeroIndexExplain() {
        NutBean context1 = _JO("{index:1}");
        WnExplain wx1 = WnExplains.parse("->list.${index}");
        String out1 = (String) wx1.explain(context1);
        assertEquals("list.1", out1);

        NutBean context2 = _JO("{index:0}");
        WnExplain wx2 = WnExplains.parse("->list.${index}");
        String out2 = (String) wx2.explain(context2);
        assertEquals("list.0", out2);
    }
}
