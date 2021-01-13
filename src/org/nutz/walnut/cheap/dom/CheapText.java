package org.nutz.walnut.cheap.dom;

public class CheapText extends CheapNode {

    protected String text;

    protected CheapText() {
        this(null);
    }

    protected CheapText(String text) {
        this.type = CheapNodeType.TEXT;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public CheapText appendText(String text) {
        if (null == text) {
            this.text = text;
        } else {
            this.text += text;
        }
        return this;
    }

    public CheapText appendLine(String text) {
        if (null == text) {
            this.text = text;
        } else {
            this.text += text;
        }
        return appendText("\n" + text);
    }
}
