package org.nutz.walnut.cheap.dom;

public class CheapText extends CheapNode {

    private String text;

    public CheapText() {
        this(null);
    }

    public CheapText(String text) {
        this.type = CheapNodeType.TEXT;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
