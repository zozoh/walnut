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

    public CheapComment appendText(String text) {
        return (CheapComment) super.appendText(text);
    }

    public CheapComment appendLine(String text) {
        return (CheapComment) super.appendLine(text);
    }

}
