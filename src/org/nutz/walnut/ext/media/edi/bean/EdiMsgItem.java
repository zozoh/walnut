package org.nutz.walnut.ext.media.edi.bean;

public abstract class EdiMsgItem {

    protected EdiMsgAdvice advice;

    public EdiMsgItem(EdiMsgAdvice advice) {
        this.advice = advice;
    }

    public abstract void joinString(StringBuilder sb);

    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.joinString(sb);
        return sb.toString();
    }

    public EdiMsgAdvice getAdvice() {
        return advice;
    }

    public void setAdvice(EdiMsgAdvice advice) {
        this.advice = advice;
    }
}
