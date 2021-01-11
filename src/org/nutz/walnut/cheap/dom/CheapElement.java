package org.nutz.walnut.cheap.dom;

public class CheapElement extends CheapNode {

    private String tagName;

    public CheapElement(String tagName) {
        this.tagName = tagName;
        this.type = CheapNodeType.ELEMENT;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

}
