package org.nutz.walnut.ooml;

import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.xml.CheapXmlParsing;

public class Oomls {

    public static CheapDocument parseEntryAsXml(OomlEntry en) {
        String str = en.getContentStr();
        CheapDocument doc = new CheapDocument(null);
        CheapXmlParsing ing = new CheapXmlParsing(doc);
        ing.parseDoc(str);
        return doc;
    }

}
