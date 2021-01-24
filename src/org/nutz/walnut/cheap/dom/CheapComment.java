package org.nutz.walnut.cheap.dom;

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
    public void joinString(StringBuilder sb) {
        sb.append("<!--");
        sb.append(text);
        sb.append("-->");
    }

    public void joinText(StringBuilder sb) {}

}
