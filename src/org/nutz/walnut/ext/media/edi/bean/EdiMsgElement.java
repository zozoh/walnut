package org.nutz.walnut.ext.media.edi.bean;

import org.nutz.walnut.util.Ws;

public class EdiMsgElement {

    private EdiMsgElementType type;

    private String value;

    public EdiMsgElement(String input) {
        String s = null == input ? null : input.trim();
        if (Ws.isBlank(s)) {
            type = EdiMsgElementType.EMPTY;
            value = null;
        }
        // 特殊标记
        else if(s.matches("^[A-Z]{2,3}$")) {
            type = EdiMsgElementType.TAG;
            value = s;
        }
        // 数字
        else if(s.matches("^[0-9].$")) {
            type = EdiMsgElementType.NUMBER;
            value = s;
        }
        // 默认就是自由文本
        else{
            type = EdiMsgElementType.TEXT;
            value = s;
        }
    }

    public EdiMsgElementType getType() {
        return type;
    }

    public void setType(EdiMsgElementType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
