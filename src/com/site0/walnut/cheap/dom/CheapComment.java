package com.site0.walnut.cheap.dom;

public class CheapComment extends CheapText {

    protected CheapComment() {
        this(null);
    }

    protected CheapComment(String text) {
        this.type = CheapNodeType.COMMENT;
        this.text = text;
        this.treeDisplayName = "<!--";
    }

    @Override
    public CheapComment clone() {
        return this.cloneNode();
    }

    @Override
    public CheapComment cloneNode() {
        return new CheapComment(this.text);
    }

    @Override
    public void joinString(StringBuilder sb) {
        sb.append("<!--");
        sb.append(text);
        sb.append("-->");
    }

    public void joinText(StringBuilder sb) {}

}
