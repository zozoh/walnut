package org.nutz.walnut.ext.media.edi.bean;

import org.nutz.walnut.util.Ws;

public class EdiElement {

    private EdiElementType type;

    private String value;

    public EdiElement(EdiElementType type, String val) {
        this.type = type;
        this.value = val;
    }

    public EdiElement(String input) {
        String s = null == input ? null : input.trim();
        if (Ws.isBlank(s)) {
            type = EdiElementType.EMPTY;
            value = null;
        }
        // 特殊标记
        else if (s.matches("^[A-Z]{2,10}$")) {
            type = EdiElementType.TAG;
            value = s;
        }
        // 数字
        else if (s.matches("^[0-9].$")) {
            type = EdiElementType.NUMBER;
            value = s;
        }
        // 默认就是自由文本
        else {
            type = EdiElementType.TEXT;
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
        if (EdiElementType.TAG == this.type) {
            if (null == value) {
                return false;
            }
            if (name.startsWith("^")) {
                return value.matches(name);
            }
            return value.equals(name);
        }
        return false;
    }

    public boolean isTag() {
        return EdiElementType.TAG == this.type;
    }

    public boolean isText() {
        return EdiElementType.TEXT == this.type;
    }

    public boolean isEmpty() {
        return EdiElementType.EMPTY == this.type;
    }

    public boolean isNumber() {
        return EdiElementType.NUMBER == this.type;
    }

    public EdiElementType getType() {
        return type;
    }

    public void setType(EdiElementType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
