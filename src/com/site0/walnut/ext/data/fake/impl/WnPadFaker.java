package com.site0.walnut.ext.data.fake.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.site0.walnut.ext.data.fake.WnFaker;
import com.site0.walnut.ext.data.fake.WnFakes;
import com.site0.walnut.util.Ws;

public class WnPadFaker implements WnFaker<String> {

    private enum PadMode {
        START, END

    }

    private WnFaker<?> faker;

    private int width;

    private PadMode mode;

    private char padChar;

    public WnPadFaker(WnFaker<?> faker) {
        this.faker = faker;
        this.width = 0;
        this.mode = PadMode.START;
        this.padChar = ' ';
    }

    /**
     * 根据输入字符串解析模拟配置
     * 
     * @param input
     * 
     *            <pre>
     *   +----开头的 PAD 是可选的
     *   |  +---- 居左还是居右，可以是 [LR]
     *   V  V
     * PAD[6R0]
     *     ^ ^
     *     | +---- 采用什么字符填充，默认是 0 
     *     +--- 数字表示填充的宽度
     *            </pre>
     * 
     * @return 自身
     */
    public WnPadFaker valueOf(String input, String lang) {
        Matcher m = _P.matcher(input);
        if (m.find()) {
            this.width = Integer.parseInt(m.group(2));
            // L:居于左侧; R: 居于右侧
            this.mode = "R".equals(m.group(3)) ? PadMode.START : PadMode.END;
            String cs = m.group(4);
            if (null != cs && cs.length() > 0) {
                this.padChar = cs.charAt(0);
            } else {
                this.padChar = ' ';
            }
            String s_faker = Ws.trim(m.group(6));
            if (!Ws.isBlank(s_faker)) {
                this.faker = WnFakes.createFaker(s_faker, lang);
            }
        }
        return this;
    }

    private static final String regex = "^(PAD)?\\[([0-9]+)([LR])(.)?\\](:(.+))?";
    private static final Pattern _P = Pattern.compile(regex);

    @Override
    public String next() {
        Object v = this.faker.next();
        String s = v.toString();
        if (width <= 0) {
            return s;
        }
        if (this.mode == PadMode.START) {
            return Ws.padStart(s, width, padChar);
        }
        return Ws.padEnd(s, width, padChar);
    }

    public WnFaker<?> getFaker() {
        return faker;
    }

    public void setFaker(WnFaker<?> faker) {
        this.faker = faker;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public PadMode getMode() {
        return mode;
    }

    public void setMode(PadMode align) {
        this.mode = align;
    }

    public char getPadChar() {
        return padChar;
    }

    public void setPadChar(char padChar) {
        this.padChar = padChar;
    }

}
