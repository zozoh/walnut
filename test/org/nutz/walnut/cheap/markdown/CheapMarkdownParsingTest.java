package org.nutz.walnut.cheap.markdown;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.walnut.cheap.AbstractCheapParsingTest;
import org.nutz.walnut.cheap.dom.CheapDocument;

public class CheapMarkdownParsingTest extends AbstractCheapParsingTest {

    @Test
    public void test_02() {
        String input = _Fmd("02");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml("  ");

        String expect = _Fhtml("02");
        assertEquals(expect, html);

        html = doc.toHtml("  ");
        assertEquals(expect, html);

        html = doc.toHtml("  ");
        assertEquals(expect, html);
    }

    @Test
    public void test_01() {
        String input = _Fmd("01");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml("  ");

        String expect = _Fhtml("01");
        assertEquals(expect, html);

        html = doc.toHtml("  ");
        assertEquals(expect, html);

        html = doc.toHtml("  ");
        assertEquals(expect, html);
    }

    @Test
    public void test_00() {
        String input = _Fmd("00");
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(input);

        String html = doc.toHtml("  ");

        String expect = _Fhtml("00");
        assertEquals(expect, html);

        html = doc.toHtml("  ");
        assertEquals(expect, html);

        html = doc.toHtml("  ");
        assertEquals(expect, html);
    }

}
