package org.nutz.walnut.ext.media.edi.bean;

import org.nutz.walnut.util.Ws;

public class EdiMsgElement {

    private EdiMsgElementType type;

    private Object value;

    public EdiMsgElement(EdiMsgElementType type, Object val) {
        this.type = type;
        this.value = val;
        if (null == val) {
            this.type = EdiMsgElementType.EMPTY;
        } else if (val instanceof Number) {
            this.type = EdiMsgElementType.NUMBER;
        }
    }

    public EdiMsgElement(String input) {
        String s = null == input ? null : input.trim();
        if (Ws.isBlank(s)) {
            type = EdiMsgElementType.EMPTY;
            value = null;
        }
        // 特殊标记
        else if (s.matches("^[A-Z]{2,3}$")) {
            type = EdiMsgElementType.TAG;
            value = s;
        }
        // 数字
        else if (s.matches("^[0-9].$")) {
            type = EdiMsgElementType.NUMBER;
            value = Integer.parseInt(s);
        }
        // 默认就是自由文本
        else {
            type = EdiMsgElementType.TEXT;
            value = s;
        }
    }

    public void joinString(StringBuilder sb) {
        if (null != this.value) {
            sb.append(this.value);
        }

    }

    public String toString() {
        return String.format("<%s>:%s", this.type, this.value);
    }

    public boolean isTag(String name) {
        if (EdiMsgElementType.TAG == this.type) {
            return null != value && value.equals(name);
        }
        return false;
    }

    public boolean isTag() {
        return EdiMsgElementType.TAG == this.type;
    }

    public boolean isText() {
        return EdiMsgElementType.TEXT == this.type;
    }

    public boolean isEmpty() {
        return EdiMsgElementType.EMPTY == this.type;
    }

    public boolean isNumber() {
        return EdiMsgElementType.NUMBER == this.type;
    }

    public EdiMsgElementType getType() {
        return type;
    }

    public void setType(EdiMsgElementType type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
