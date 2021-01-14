package org.nutz.walnut.cheap.dom;

import java.util.regex.Pattern;

import org.nutz.walnut.util.Ws;

public class CheapRawData extends CheapNode {

    private String data;

    protected CheapRawData() {
        this(null);
    }

    protected CheapRawData(String data) {
        this.type = CheapNodeType.RAW_DATA;
        this.data = data;
    }

    @Override
    public void joinTree(StringBuilder sb, int depth, String tab) {
        sb.append(Ws.repeat(tab, depth));
        sb.append(String.format("|-- [%d]<![CDATA: ", this.getNodeIndex()));
        // 空
        if (null == data) {
            sb.append("NULL");
        }
        // 字符串
        else {
            int maxLen = 40 - (depth * tab.length());
            String s = data.replaceAll("\r?\n", "⬅️");
            if (s.length() > maxLen) {
                sb.append('[');
                sb.append(s.substring(0, maxLen));
                sb.append("...");
                sb.append(']');
            }
            // 直接显示
            else {
                sb.append('[').append(s).append(']');
            }
        }
        sb.append("\n");
    }

    @Override
    public void format(String tab, int depth, Pattern newLineTag) {}

    @Override
    public boolean isEmpty() {
        return Ws.isEmpty(data);
    }

    @Override
    public void joinString(StringBuilder sb) {
        sb.append("<![CDATA[");
        sb.append(data);
        sb.append("]]>");
    }

    public String getData() {
        return data;
    }

    public void setData(String content) {
        this.data = content;
    }

}
