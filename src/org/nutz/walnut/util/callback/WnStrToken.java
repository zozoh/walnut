package org.nutz.walnut.util.callback;

public class WnStrToken {

    /**
     * 类型，根据解析器不同有不同的含义，譬如:
     * 
     * @see org.nutz.walnut.util.callback.WnStrTokenType
     */
    public WnStrTokenType type;

    /**
     * 处理者上次处理的原始内容
     */
    // public int offset;

    /**
     * 表示引号的字符
     */
    public char quoteC;

    /**
     * 符号的内容
     */
    public StringBuilder text;

    /**
     * 当前下标
     */
    public int index;

    /**
     * 原始字符串内容
     */
    public char[] src;

    public boolean hasText() {
        return text.length() > 0;
    }

    public void reset(char c) {
        text.delete(0, text.length());
        // offset = index + 1;
        this.quoteC = c;
    }

}
