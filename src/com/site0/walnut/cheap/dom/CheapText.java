package com.site0.walnut.cheap.dom;

import com.site0.walnut.util.Ws;

public class CheapText extends CheapRawData {

    protected CheapText() {
        this(null);
    }

    protected CheapText(String text) {
        this(text, false);
    }

    protected CheapText(String text, boolean asRaw) {
        this.type = CheapNodeType.TEXT;
        // 这里需要对文字进行关键字逃逸
        this.text = asRaw ? text : Ws.escapeHTML(text);
        this.treeDisplayName = "!TEXT";
        this.treeDisplayLeftQuoteChar = '"';
        this.treeDisplayRightQuoteChar = '"';
    }

    @Override
    public CheapText clone() {
        return this.cloneNode();
    }

    @Override
    public CheapText cloneNode() {
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
