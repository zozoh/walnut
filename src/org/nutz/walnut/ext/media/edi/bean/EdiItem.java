package org.nutz.walnut.ext.media.edi.bean;

public abstract class EdiItem {

    protected EdiAdvice advice;

    public EdiItem(EdiAdvice advice) {
        this.advice = advice;
    }

    public abstract void joinString(StringBuilder sb);

    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.joinString(sb);
        return sb.toString();
    }

    public EdiAdvice getAdvice() {
        return advice;
    }

    public void setAdvice(EdiAdvice advice) {
        this.advice = advice;
    }
}
