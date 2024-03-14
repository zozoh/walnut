package com.site0.walnut.cheap.dom.mutation;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.json.Json;
import com.site0.walnut.cheap.AbstractCheapParsingTest;
import com.site0.walnut.cheap.dom.CheapDocument;
import com.site0.walnut.cheap.xml.CheapXmlParsing;

public class CheapDomOperationTest extends AbstractCheapParsingTest {

    @Test
    public void test_01() {
        String input = _Fhtml("mutate_1_input");
        String expect = _Fhtml("mutate_1_expect");
        String json = _Fjson("opt_1");

        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        CheapDomOperation opt = Json.fromJson(CheapDomOperation.class, json);
        doc.change(opt);
        doc.removeBlankNodes();
        doc.formatAsHtml();

        String str = doc.toMarkup();
        assertEquals(expect, str);
    }

    @Test
    public void test_00() {
        String input = _Fhtml("mutate_0_input");
        String expect = _Fhtml("mutate_0_expect");
        String json = _Fjson("opt_0");

        CheapXmlParsing ing = new CheapXmlParsing();
        CheapDocument doc = ing.parseDoc(input);

        CheapDomOperation opt = Json.fromJson(CheapDomOperation.class, json);
        doc.change(opt);

        String str = doc.toMarkup();
        assertEquals(expect, str);
    }

}
