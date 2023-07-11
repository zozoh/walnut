package org.nutz.walnut.ext.media.edi.bean;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.tmpl.WnTmplX;

public class EdiInterchangeTest {

    private String _read_input(String name) {
        String txt = Files.read("org/nutz/walnut/ext/media/edi/bean/pack/" + name + ".edi.txt");
        return txt.replaceAll("\r\n", "\n").trim();
    }

    @Test
    public void test_UCI() {
        String input = _read_input("c_error");
        EdiInterchange ic = EdiInterchange.parse(input);

        NutMap bean = new NutMap();
        EdiSegment UCI = ic.getFirstEntry().findSegment("UCI");
        UCI.fillBean(bean, "tagName", "crn", "Creator,,Owner", "Recipient", "code");
        assertEquals("UCI", bean.getString("tagName"));
        assertEquals("23062600000024", bean.getString("crn"));
        assertEquals("AAR399A", bean.getString("Creator"));
        assertEquals("AAR399A", bean.getString("Owner"));
        assertEquals("AAA336C", bean.getString("Recipient"));
        assertEquals("4", bean.getString("code"));
        
        //
        //  测试成功
        //
        input = _read_input("c_ok");
        ic = EdiInterchange.parse(input);

        bean = new NutMap();
        UCI = ic.getFirstEntry().findSegment("UCI");
        UCI.fillBean(bean, "tagName", "crn", "Creator,,Owner", "Recipient", "code");
        assertEquals("UCI", bean.getString("tagName"));
        assertEquals("23062700000009", bean.getString("crn"));
        assertEquals("AAR399A", bean.getString("Creator"));
        assertEquals("AAR399A", bean.getString("Owner"));
        assertEquals("AAA336C", bean.getString("Recipient"));
        assertEquals("7", bean.getString("code"));
    }

    @Test
    public void test_00_fillBean() {
        String input = _read_input("c00");
        EdiInterchange ic = EdiInterchange.parse(input);
        ic.packEntry();

        NutMap bean = new NutMap();
        EdiSegment head = ic.getHead();
        head.fillBean(bean, "type", "part: p1,p2", null, null, null, "n1", null, "n2");
        assertEquals("UNB", bean.getString("type"));
        NutMap part = bean.getAs("part", NutMap.class);
        assertEquals("A", part.getString("p1"));
        assertEquals("B", part.getString("p2"));
        assertEquals(1, bean.getInt("n1"));
        assertEquals(1, bean.getInt("n2"));

        EdiMessage en = ic.getFirstEntry();
        bean = new NutMap();
        head = en.getHead();
        head.fillBean(bean, "type", "nb");
        assertEquals("UNH", bean.getString("type"));
        assertEquals(1, bean.getInt("nb"));

        EdiSegment rff = en.findSegment("RFF");
        bean = new NutMap();
        rff.fillBean(bean, "type", "info: type,abn");
        assertEquals("RFF", bean.getString("type"));
        NutMap info = bean.getAs("info", NutMap.class);
        assertEquals("ABN", info.getString("type"));
        assertEquals("15654834214", info.getString("abn"));
    }

    @Test
    public void test_00() {
        String input = _read_input("c00");
        EdiInterchange ic = EdiInterchange.parse(input);
        String s2 = ic.toString().trim();
        assertEquals(input, s2);

        String in2 = WnTmplX.exec(input, Wlang.map("N", 3));
        ic.packEntry();
        String s3 = ic.toString().trim();
        assertEquals(in2, s3);
    }

    @Test
    public void test_01() {
        String input = _read_input("c01");
        EdiInterchange ic = EdiInterchange.parse(input);
        String s2 = ic.toString().trim();
        assertEquals(input, s2);

        String in2 = WnTmplX.exec(input, Wlang.map("N", 10));
        ic.packEntry();
        String s3 = ic.toString().trim();
        assertEquals(in2, s3);
    }

    @Test
    public void test_02() {
        String input = _read_input("c02");
        EdiInterchange ic = EdiInterchange.parse(input);
        String s2 = ic.toString().trim();
        assertEquals(input, s2);

        String in2 = WnTmplX.exec(input, Wlang.map("N", 4));
        ic.packEntry();
        String s3 = ic.toString().trim();
        assertEquals(in2, s3);
    }

    @Test
    public void test_03() {
        String input = _read_input("c03");
        EdiInterchange ic = EdiInterchange.parse(input);
        String s2 = ic.toString().trim();
        assertEquals(input, s2);

        String in2 = WnTmplX.exec(input, Wlang.map("N", 57));
        ic.packEntry();
        String s3 = ic.toString().trim();
        assertEquals(in2, s3);
    }

}
