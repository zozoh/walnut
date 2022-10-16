package org.nutz.walnut.cheap.dom;

import org.nutz.walnut.util.Ws;

public class CheapText extends CheapRawData {

    protected CheapText() {
        this(null);
    }

    protected CheapText(String text) {
        this.type = CheapNodeType.TEXT;
        this.text = text;
        this.treeDisplayName = "!TEXT";
        this.treeDisplayLeftQuoteChar = '"';
        this.treeDisplayRightQuoteChar = '"';
    }

    @Override
    public CheapText clone() {
        return this.cloneSelf();
    }

    @Override
    public CheapText cloneSelf() {
        return new CheapText(this.text);
    }

    @Override
    public void format(CheapFormatter cdf, int depth) {
        if (!this.isFormated() && this.isPlacehold()) {
            // 准备格式化文本
            if (null != this.next) {
                this.text = cdf.getPrefix(depth);
            }
            // 最后一个节点，需要回退一级缩进
            else {
                this.text = cdf.getPrefix(depth - 1);
            }
            // 标识格式化，以便幂等
            this.setFormatted(true);
        }
    }

    public String decodeText() {
        return Ws.decodeHtmlEntities(this.text);
    }

    @Override
    public void joinString(StringBuilder sb) {
        sb.append(text);
    }

}
