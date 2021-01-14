package org.nutz.walnut.cheap.html;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.walnut.cheap.AbstractCheapParsingTest;
import org.nutz.walnut.cheap.dom.CheapDocument;

public class CheapHtmlParsingTest extends AbstractCheapParsingTest {

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
