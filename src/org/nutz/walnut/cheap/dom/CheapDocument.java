package org.nutz.walnut.cheap.dom;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

public class CheapDocument {

    private NutBean header;

    private CheapElement $root;

    private CheapElement $body;

    public CheapDocument() {
        this("doc", "body");
    }

    public CheapDocument(String rootTagName, String bodyTagName) {
        header = new NutMap();
        $root = new CheapElement(rootTagName);
        $root.ownerDocument = this;
        $body = new CheapElement(bodyTagName);
        $body.appendTo($root);
        $root.rebuildChildren();
    }

    public CheapElement createElement(String tagName) {
        return createElement(tagName, null);
    }

    public CheapElement createElement(String tagName, String className) {
        CheapElement $node = new CheapElement(tagName, className);
        $node.ownerDocument = this;
        return $node;
    }

    public CheapText createTextNode() {
        return createTextNode(null);
    }

    public CheapText createTextNode(String text) {
        CheapText $node = new CheapText(text);
        $node.ownerDocument = this;
        return $node;
    }

    public CheapComment createComment() {
        return createComment(null);
    }

    public CheapComment createComment(String text) {
        CheapComment $node = new CheapComment(text);
        $node.ownerDocument = this;
        return $node;
    }

    public CheapRawData createRawData() {
        return createRawData(null);
    }

    public CheapRawData createRawData(String data) {
        CheapRawData $node = new CheapRawData(data);
        $node.ownerDocument = this;
        return $node;
    }

    public NutBean getHeader() {
        return header;
    }

    public void setHeader(NutBean headers) {
        this.header = headers;
    }

    public CheapElement root() {
        return this.$root;
    }

    public CheapElement body() {
        return this.$body;
    }

    public void ready() {
        this.$root.rebuildChildren();
    }

}
