package org.nutz.walnut.cheap.dom;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

public class CheapDocument {

    private NutBean header;

    private CheapElement rootElement;

    public CheapDocument() {
        this("doc");
    }

    public CheapDocument(String rootTagName) {
        header = new NutMap();
        rootElement = new CheapElement(rootTagName);
    }

    public NutBean getHeader() {
        return header;
    }

    public void setHeader(NutBean headers) {
        this.header = headers;
    }

    public CheapElement getRootElement() {
        return rootElement;
    }

    public void setRootElement(CheapElement rootElement) {
        this.rootElement = rootElement;
    }

}
