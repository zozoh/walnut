package org.nutz.walnut.cheap.html;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.walnut.cheap.AbstractCheapParsingTest;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;

public class CheapHtmlParsingTest extends AbstractCheapParsingTest {

    @Test
    public void test_xml_1() {
        String input = _Fxml("t_xml_1");
        CheapHtmlParsing ing = new CheapHtmlParsing(false);
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);

        CheapElement el = doc.findElement(e -> "w:qFormat".equals(e.getTagName()));
        assertNotNull(el);
        assertEquals("w:qFormat", el.getTagName());
    }

    @Test
    public void test_xml_0() {
        String input = _Fxml("t_xml_0");
        CheapHtmlParsing ing = new CheapHtmlParsing(false);
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_t0() {
        String input = _Fhtml("t0_in");
        String expec = _Fhtml("t0_ex");
        CheapHtmlParsing ing = new CheapHtmlParsing(true);
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml();
        assertEquals(expec, html);
    }

    @Test
    public void test_node_nest_text() {
        String input = "<p><strong>ABC</strong></p>";
        CheapHtmlParsing ing = new CheapHtmlParsing(true);
        CheapDocument doc = ing.invoke(input);

        String text = doc.body().getText();
        assertEquals("ABC", text);
    }

    @Test
    public void test_100() {
        String input = _Fhtml("100");
        CheapHtmlParsing ing = new CheapHtmlParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_15() {
        String input = _Fhtml("15");
        CheapHtmlParsing ing = new CheapHtmlParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_14() {
        String input = _Fhtml("14");
        CheapHtmlParsing ing = new CheapHtmlParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_13() {
        String input = _Fhtml("13");
        CheapHtmlParsing ing = new CheapHtmlParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_12() {
        String input = _Fhtml("12");
        CheapHtmlParsing ing = new CheapHtmlParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_11() {
        String input = _Fhtml("11");
        CheapHtmlParsing ing = new CheapHtmlParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_10() {
        String input = _Fhtml("10");
        CheapHtmlParsing ing = new CheapHtmlParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_09() {
        String input = _Fhtml("09");
        CheapHtmlParsing ing = new CheapHtmlParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_08() {
        String input = _Fhtml("08");
        CheapHtmlParsing ing = new CheapHtmlParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_07() {
        String input = _Fhtml("07");
        CheapHtmlParsing ing = new CheapHtmlParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_06() {
        String input = _Fhtml("06");
        CheapHtmlParsing ing = new CheapHtmlParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_05() {
        String input = _Fhtml("05");
        CheapHtmlParsing ing = new CheapHtmlParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_04() {
        String input = _Fhtml("04");
        CheapHtmlParsing ing = new CheapHtmlParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_03() {
        String input = _Fhtml("03");
        CheapHtmlParsing ing = new CheapHtmlParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_02() {
        String input = _Fhtml("02");
        CheapHtmlParsing ing = new CheapHtmlParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_01() {
        String input = _Fhtml("01");
        CheapHtmlParsing ing = new CheapHtmlParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

    @Test
    public void test_00() {
        String input = _Fhtml("00");
        CheapHtmlParsing ing = new CheapHtmlParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toMarkup();
        assertEquals(input, html);
    }

}
