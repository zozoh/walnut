package org.nutz.walnut.cheap.dom;

public class CheapComment extends CheapText {

    protected CheapComment() {
        this(null);
    }

    protected CheapComment(String text) {
        this.type = CheapNodeType.COMMENT;
        this.text = text;
    }

    public CheapComment appendText(String text) {
        return (CheapComment) super.appendText(text);
    }

    public CheapComment appendLine(String text) {
        return (CheapComment) super.appendLine(text);
    }

}
