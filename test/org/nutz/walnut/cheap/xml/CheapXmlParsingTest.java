package org.nutz.walnut.cheap.xml;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.nutz.walnut.cheap.AbstractCheapParsingTest;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;

public class CheapXmlParsingTest extends AbstractCheapParsingTest {

    @Test
    public void test_select_3() {
        String input = _Fhtml("select_0");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        List<CheapElement> els = doc.selectAll("p.as-p");
        assertEquals(4, els.size());
        assertEquals("as-p a", els.get(0).getClassName());
        assertEquals("as-p b", els.get(1).getClassName());
        assertEquals("as-p c", els.get(2).getClassName());
        assertEquals("as-p d", els.get(3).getClassName());
    }

    @Test
    public void test_select_2() {
        String input = _Fhtml("select_0");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        List<CheapElement> els = doc.selectAll("p.a, p.c, p.b");
        assertEquals(3, els.size());
        assertEquals("as-p a", els.get(0).getClassName());
        assertEquals("A", els.get(0).getText());
        assertEquals("as-p c", els.get(1).getClassName());
        assertEquals("C", els.get(1).getText());
        assertEquals("as-p b", els.get(2).getClassName());
        assertEquals("B", els.get(2).getText());
    }

    @Test
    public void test_select_1() {
        String input = _Fhtml("select_0");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        List<CheapElement> els = doc.selectAll("blockquote > blockquote");
        assertEquals(1, els.size());
        assertEquals("at-in", els.get(0).getClassName());
    }

    @Test
    public void test_select_0() {
        String input = _Fhtml("select_0");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        List<CheapElement> els = doc.selectAll("blockquote");
        assertEquals(2, els.size());
        assertEquals("at-out", els.get(0).getClassName());
        assertEquals("at-in", els.get(1).getClassName());
    }

    @Test
    public void test_table_0() {
        String input = _Fhtml("t0_table");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_xml_3() {
        String input = _Fxml("t_xml_3");
        CheapXmlParsing ing = new CheapXmlParsing("xml");
        CheapDocument doc = ing.parseDoc(input);

        String xml = doc.toMarkup();
        assertEquals(input, xml);
    }

    @Test
    public void test_xml_2() {
        String input = _Fxml("t_xml_2");
        CheapXmlParsing ing = new CheapXmlParsing("xml");
        CheapDocument doc = ing.parseDoc(input);

        String xml = doc.toMarkup();
        assertEquals(input, xml);

        String innerXml = _Fxml("t_xml_2_inner");
        CheapElement el = doc.findElement(e -> e.isStdTagName("PET"));
        el.setInnerXML(innerXml);

        doc.formatAsXml();

        String expect = _Fxml("t_xml_2_expect");
        xml = doc.toMarkup();
        assertEquals(expect, xml);
    }

    @Test
    public void test_xml_1() {
        String input = _Fxml("t_xml_1");
        CheapXmlParsing ing = new CheapXmlParsing("w:style");
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);

        CheapElement el = doc.findElement(e -> "w:qFormat".equals(e.getTagName()));
        assertNotNull(el);
        assertEquals("w:qFormat", el.getTagName());
    }

    @Test
    public void test_xml_0() {
        String input = _Fxml("t_xml_0");
        CheapXmlParsing ing = new CheapXmlParsing("xml");
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_t0() {
        String input = _Fhtml("t0_in");
        String expec = _Fhtml("t0_ex");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toHtml();
        assertEquals(expec, html);
    }

    @Test
    public void test_node_nest_text() {
        String input = "<p><strong>ABC</strong></p>";
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String text = doc.body().getText();
        assertEquals("ABC", text);
    }

    @Test
    public void test_100() {
        String input = _Fhtml("100");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_15() {
        String input = _Fhtml("15");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_14() {
        String input = _Fhtml("14");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_13() {
        String input = _Fhtml("13");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_12() {
        String input = _Fhtml("12");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_11() {
        String input = _Fhtml("11");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_10() {
        String input = _Fhtml("10");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_09() {
        String input = _Fhtml("09");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_08() {
        String input = _Fhtml("08");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_07() {
        String input = _Fhtml("07");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_06() {
        String input = _Fhtml("06");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_05() {
        String input = _Fhtml("05");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_04() {
        String input = _Fhtml("04");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_03() {
        String input = _Fhtml("03");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_02() {
        String input = _Fhtml("02");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_01() {
        String input = _Fhtml("01");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_00() {
        String input = _Fhtml("00");
        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

}
