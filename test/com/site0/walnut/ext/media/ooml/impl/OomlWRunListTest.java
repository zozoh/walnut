package com.site0.walnut.ext.media.ooml.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.cheap.dom.CheapDocument;
import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.cheap.xml.CheapXmlParsing;

public class OomlWRunListTest {

    static String basePh = "com/site0/walnut/ext/media/ooml/impl/data/";

    @Test
    public void test_checkbox() {
        // 读取 XML
        String xIn = Files.read(basePh + "t_checkbox.xml");
        CheapDocument xInDoc = new CheapDocument(null);
        CheapXmlParsing ing = new CheapXmlParsing(xInDoc);
        xInDoc = ing.parseDoc(xIn);

        // 读取 JSON
        String xVars = Files.read(basePh + "t_checkbox.json");
        NutMap vars = Json.fromJson(NutMap.class, xVars);

        // 执行
        OomlWRunList rl = new OomlWRunList();
        CheapElement eP = xInDoc.findElement(el -> el.isTag("w:p"));

        assertEquals(16, rl.load(eP));
        assertEquals(2, rl.prepare());
        assertEquals("${重制#checkbox==true}[1:0][5:0]", rl.getPlaceholder(0).toBrief());
        assertEquals("${重制#checkbox==false}[8:0][14:0]", rl.getPlaceholder(1).toBrief());

        rl.explain(vars);
        xInDoc.formatAsXml();
        xInDoc.compactWithEl(el -> !el.isAttr("xml:space", "preserve"));
        String xmlOut = xInDoc.toMarkup();

        // 读取预期 XML
        String xEx = Files.read(basePh + "t_checkbox_ex.xml");
        CheapDocument xExDoc = new CheapDocument(null);
        ing = new CheapXmlParsing(xExDoc);
        xExDoc = ing.parseDoc(xEx);

        // 判断
        xExDoc.compactWithEl(el -> !el.isAttr("xml:space", "preserve"));
        String xmlExp = xExDoc.toMarkup();
        assertEquals(xmlExp, xmlOut);
    }

    @Test
    public void test_none() {
        // 读取 XML
        String xIn = Files.read(basePh + "t_none.xml");
        CheapDocument xInDoc = new CheapDocument(null);
        CheapXmlParsing ing = new CheapXmlParsing(xInDoc);
        xInDoc = ing.parseDoc(xIn);

        // 读取 JSON
        String xVars = Files.read(basePh + "t_normal.json");
        NutMap vars = Json.fromJson(NutMap.class, xVars);

        // 执行
        OomlWRunList rl = new OomlWRunList();
        CheapElement eP = xInDoc.findElement(el -> el.isTag("w:p"));

        assertEquals(1, rl.load(eP));
        assertEquals(0, rl.prepare());

        rl.explain(vars);
        xInDoc.compact();
        String xmlOut = xInDoc.toMarkup();

        // 读取预期 XML
        String xEx = Files.read(basePh + "t_none.xml");
        CheapDocument xExDoc = new CheapDocument(null);
        ing = new CheapXmlParsing(xExDoc);
        xExDoc = ing.parseDoc(xEx);

        // 判断
        xExDoc.compact();
        String xmlExp = xExDoc.toMarkup();
        assertEquals(xmlExp, xmlOut);
    }

    @Test
    public void test_normal() {
        // 读取 XML
        String xIn = Files.read(basePh + "t_normal.xml");
        CheapDocument xInDoc = new CheapDocument(null);
        CheapXmlParsing ing = new CheapXmlParsing(xInDoc);
        xInDoc = ing.parseDoc(xIn);

        // 读取 JSON
        String xVars = Files.read(basePh + "t_normal.json");
        NutMap vars = Json.fromJson(NutMap.class, xVars);

        // 执行
        OomlWRunList rl = new OomlWRunList();
        CheapElement eP = xInDoc.findElement(el -> el.isTag("w:p"));

        assertEquals(10, rl.load(eP));
        assertEquals(2, rl.prepare());
        assertEquals("${申请人名称中文}[0:0][4:0]", rl.getPlaceholder(0).toBrief());
        assertEquals("${哈哈哈}[7:1][9:0]", rl.getPlaceholder(1).toBrief());

        rl.explain(vars);
        xInDoc.compact();
        String xmlOut = xInDoc.toMarkup();

        // 读取预期 XML
        String xEx = Files.read(basePh + "t_normal_ex.xml");
        CheapDocument xExDoc = new CheapDocument(null);
        ing = new CheapXmlParsing(xExDoc);
        xExDoc = ing.parseDoc(xEx);

        // 判断
        xExDoc.compact();
        String xmlExp = xExDoc.toMarkup();
        assertEquals(xmlExp, xmlOut);
    }

}
