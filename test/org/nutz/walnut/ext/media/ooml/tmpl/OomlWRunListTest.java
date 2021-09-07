package org.nutz.walnut.ext.media.ooml.tmpl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.xml.CheapXmlParsing;

public class OomlWRunListTest {

    static String basePh = "org/nutz/walnut/ext/media/ooml/tmpl/data/";

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
