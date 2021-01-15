package org.nutz.walnut.cheap.markdown;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.walnut.cheap.AbstractCheapParsingTest;
import org.nutz.walnut.cheap.dom.CheapDocument;

public class CheapMarkdownParsingTest extends AbstractCheapParsingTest {

    @Test
    public void test_15() {
        String input = _Fmd("15");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml();

        String expect = _Fhtml("15");
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);
    }

    @Test
    public void test_14() {
        String input = _Fmd("14");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml();

        String expect = _Fhtml("14");
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);
    }

    @Test
    public void test_13() {
        String input = _Fmd("13");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml();

        String expect = _Fhtml("13");
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);
    }

    @Test
    public void test_12() {
        String input = _Fmd("12");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml();

        String expect = _Fhtml("12");
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);
    }

    @Test
    public void test_11() {
        String input = _Fmd("11");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml();

        String expect = _Fhtml("11");
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);
    }

    @Test
    public void test_10() {
        String input = _Fmd("10");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml();

        String expect = _Fhtml("10");
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);
    }

    @Test
    public void test_09() {
        String input = _Fmd("09");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml();

        String expect = _Fhtml("09");
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);
    }

    @Test
    public void test_08() {
        String input = _Fmd("08");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml();

        String expect = _Fhtml("08");
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);
    }

    @Test
    public void test_07() {
        String input = _Fmd("07");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml();

        String expect = _Fhtml("07");
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);
    }

    @Test
    public void test_06() {
        String input = _Fmd("06");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml();

        String expect = _Fhtml("06");
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);
    }

    @Test
    public void test_05() {
        String input = _Fmd("05");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml();

        String expect = _Fhtml("05");
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);
    }

    @Test
    public void test_04() {
        String input = _Fmd("04");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml();

        String expect = _Fhtml("04");
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);
    }

    @Test
    public void test_03() {
        String input = _Fmd("03");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml();

        String expect = _Fhtml("03");
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);
    }

    @Test
    public void test_02() {
        String input = _Fmd("02");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml();

        String expect = _Fhtml("02");
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);
    }

    @Test
    public void test_01() {
        String input = _Fmd("01");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml();

        String expect = _Fhtml("01");
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);
    }

    @Test
    public void test_00() {
        String input = _Fmd("00");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml();

        String expect = _Fhtml("00");
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);
    }

    @Test
    public void test_100() {
        String input = _Fmd("100");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml();

        String expect = _Fhtml("100");
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);

        html = doc.toHtml();
        assertEquals(expect, html);
    }
}
