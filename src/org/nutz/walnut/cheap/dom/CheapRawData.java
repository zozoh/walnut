package org.nutz.walnut.cheap.dom;

import java.util.regex.Pattern;

import org.nutz.walnut.util.Ws;

public class CheapRawData extends CheapNode {

    protected String text;

    protected String treeDisplayName;

    protected char treeDisplayLeftQuoteChar;

    protected char treeDisplayRightQuoteChar;

    protected CheapRawData() {
        this(null);
    }

    protected CheapRawData(String data) {
        this.type = CheapNodeType.RAW_DATA;
        this.text = data;
        this.treeDisplayName = "<![CDATA";
        this.treeDisplayLeftQuoteChar = '[';
        this.treeDisplayRightQuoteChar = ']';
    }

    @Override
    public String toBrief() {
        return String.format("[%d]%s: ", this.getNodeIndex(), treeDisplayName);
    }

    @Override
    public void joinTree(StringBuilder sb, int depth, String tab) {
        sb.append(Ws.repeat(tab, depth));
        sb.append("|-- ");
        sb.append(this.toBrief());
        // 空
        if (null == text) {
            sb.append("NULL");
        }
        // 字符串
        else {
            int maxLen = Math.max(10, 40 - (depth * tab.length()));
            String s = text.replaceAll("\r?\n", "⬅️");
            if (s.length() > maxLen) {
                sb.append(treeDisplayLeftQuoteChar);
                sb.append(s.substring(0, maxLen));
                sb.append("...");
                sb.append(treeDisplayRightQuoteChar);
            }
            // 直接显示
            else {
                sb.append(treeDisplayLeftQuoteChar);
                sb.append(s);
                sb.append(treeDisplayRightQuoteChar);
            }
        }
        sb.append("\n");
    }

    @Override
    public void format(CheapFormatter cdf, int depth) {}

    @Override
    public boolean isEmpty() {
        return Ws.isEmpty(text);
    }

    @Override
    public boolean isBlank() {
        return Ws.isBlank(text);
    }

    @Override
    public void joinString(StringBuilder sb) {
        sb.append("<![CDATA[");
        sb.append(text);
        sb.append("]]>");
    }

    public void joinText(StringBuilder sb) {
        if (null != text)
            sb.append(text);
    }

    @Override
    public void setText(String content) {
        this.text = content;
    }

    public boolean isTextStartsWith(String str) {
        return null != text && text.startsWith(str);
    }

    public boolean isTextEndsWith(String str) {
        return null != text && text.endsWith(str);
    }

    public boolean isTextMatch(String regex) {
        return null != text && text.matches(regex);
    }

    public boolean isTextMatch(Pattern p) {
        return null != text && p.matcher(text).find();
    }

    public boolean isTextContains(String s) {
        return null != text && text.contains(s);
    }

    public void prependText(String text) {
        if (null == text) {
            this.text = text;
        } else {
            this.text = text + this.text;
        }
    }

    public void appendText(String text) {
        if (null == text) {
            this.text = text;
        } else {
            this.text += text;
        }
    }

    public void appendLine(String text) {
        if (null == text) {
            this.text = text;
        } else {
            this.text += text;
        }
        appendText("\n" + text);
    }

}
