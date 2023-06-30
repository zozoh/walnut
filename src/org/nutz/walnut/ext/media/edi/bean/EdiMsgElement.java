package org.nutz.walnut.ext.media.edi.bean;

public class EdiMsgElement {

    private EdiMsgElementType type;

    private String value;

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
