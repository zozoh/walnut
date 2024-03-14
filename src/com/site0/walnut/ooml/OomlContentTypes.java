package com.site0.walnut.ooml;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Encoding;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.cheap.dom.CheapDocument;
import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.cheap.xml.CheapXmlParsing;

public class OomlContentTypes {

    private NutMap defaults;

    private NutMap overrides;

    public OomlContentTypes() {
        this.defaults = new NutMap();
        this.overrides = new NutMap();
    }

    public OomlContentTypes(String xml) {
        this();
        this.load(xml);
    }

    public OomlContentTypes(CheapDocument doc) {
        this();
        this.load(doc);
    }

    public void load(String xml) {
        CheapDocument doc = new CheapDocument("Types");
        CheapXmlParsing parser = new CheapXmlParsing(doc);
        doc = parser.parseDoc(xml);
        this.load(doc);
    }

    public void load(CheapDocument doc) {
        CheapElement root = doc.root();
        // ...
        // <Default Extension="png" ContentType="image/png"/>
        // ...
        List<CheapElement> elList = root.getChildElements(el -> el.isStdTagName("DEFAULT"));
        for (CheapElement el : elList) {
            String key = el.attr("Extension");
            String val = el.attr("ContentType");
            defaults.put(key, val);
        }
        // ...
        // <Override PartName="/word/document.xml"
        // ContentType="applicat..nt.main+xml"/>
        // ...
        elList = root.getChildElements(el -> el.isStdTagName("OVERRIDE"));
        for (CheapElement el : elList) {
            String key = el.attr("PartName");
            String val = el.attr("ContentType");
            overrides.put(key, val);
        }
    }

    public CheapDocument toDocument() {
        CheapDocument doc = new CheapDocument("Types");
        CheapElement root = doc.root();
        root.attr("xmlns", "http://schemas.openxmlformats.org/package/2006/content-types");
        // ...
        // <Default Extension="png" ContentType="image/png"/>
        // ...
        if (null != defaults && !defaults.isEmpty()) {
            for (Map.Entry<String, Object> en : defaults.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                CheapElement el = doc.createElement("Default");
                el.attr("Extension", key);
                el.attr("ContentType", val);
                root.append(el);
            }
        }
        // ...
        // <Override PartName="/word/document.xml"
        // ContentType="applicat..nt.main+xml"/>
        // ...
        if (null != overrides && !overrides.isEmpty()) {
            for (Map.Entry<String, Object> en : overrides.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                CheapElement el = doc.createElement("Override");
                el.attr("PartName", key);
                el.attr("ContentType", val);
                root.append(el);
            }
        }
        return doc;
    }
    
    @Override
    public String toString() {
        CheapDocument doc = this.toDocument();
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sb.append(doc.toMarkup());
        return sb.toString();
    }

    public byte[] toByte() {
        return this.toByte(Encoding.CHARSET_UTF8);
    }

    public byte[] toByte(Charset charset) {
        String s = this.toString();
        return s.getBytes(charset);
    }

    public OomlContentTypes reset() {
        this.defaults.clear();
        this.overrides.clear();
        return this;
    }

    public NutMap getDefaults() {
        return defaults;
    }

    public void setDefaults(NutMap defaults) {
        this.defaults = defaults;
    }

    public NutMap getOverrides() {
        return overrides;
    }

    public void setOverrides(NutMap overrides) {
        this.overrides = overrides;
    }

}
