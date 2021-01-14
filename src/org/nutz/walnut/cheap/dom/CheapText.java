package org.nutz.walnut.cheap.dom;

import java.util.regex.Pattern;

import org.nutz.walnut.util.Ws;

public class CheapText extends CheapNode {

    protected String text;

    protected String treeDisplayName;

    protected CheapText() {
        this(null);
    }

    protected CheapText(String text) {
        this.type = CheapNodeType.TEXT;
        this.text = text;
        this.treeDisplayName = "!TEXT";
    }

    @Override
    public void joinTree(StringBuilder sb, int depth, String tab) {
        sb.append(Ws.repeat(tab, depth));
        sb.append(String.format("|-- [%d]%s: ", this.getNodeIndex(), treeDisplayName));
        // 空
        if (null == text) {
            sb.append("NULL");
        }
        // 字符串
        else {
            int maxLen = 40 - (depth * tab.length());
            String s = text.replaceAll("\r?\n", "⬅️");
            if (s.length() > maxLen) {
                sb.append('"');
                sb.append(s.substring(0, maxLen));
                sb.append("...");
                sb.append('"');
            }
            // 直接显示
            else {
                sb.append('"').append(s).append('"');
            }
        }
        sb.append("\n");
    }

    @Override
    public void format(String tab, int depth, Pattern newLineTag) {}

    @Override
    public boolean isEmpty() {
        return Ws.isEmpty(text);
    }

    @Override
    public void joinString(StringBuilder sb) {
        sb.append(text);
    }

    public boolean isTextStartsWith(String str) {
        return null != text && text.startsWith(str);
    }

    public boolean isTextEndsWith(String str) {
        return null != text && text.endsWith(str);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public CheapText prependText(String text) {
        if (null == text) {
            this.text = text;
        } else {
            this.text = text + this.text;
        }
        return this;
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
