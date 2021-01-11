package org.nutz.walnut.cheap.dom;

public class CheapComment extends CheapNode {

    private String content;

    public CheapComment() {
        this(null);
    }

    public CheapComment(String content) {
        this.type = CheapNodeType.COMMENT;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String text) {
        this.content = text;
    }

}
