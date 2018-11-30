package org.nutz.walnut.ext.tpassport;

import org.nutz.lang.Strings;

public class TPassportDrawItem {
    // key
    public String name;
    // 格式
    public int top;
    public int left;
    public int width;
    public int height;
    public int padding;
    public int margin;
    public String bgColor;
    public String font;
    public String fontColor;
    public int fontSize;
    public int fontStyle;
    public String align;
    // 内容
    public String content;

    // 绘制参数
    public String as;
    public String path;
    public String fid;

    public String getContent() {
        return Strings.isBlank(content) ? name : content;
    }
}
