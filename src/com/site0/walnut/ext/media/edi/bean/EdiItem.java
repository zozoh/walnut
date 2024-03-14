package com.site0.walnut.ext.media.edi.bean;

import org.nutz.json.JsonField;

public abstract class EdiItem {

    @JsonField(ignore = true)
    protected EdiAdvice advice;

    public EdiItem(EdiAdvice advice) {
        this.advice = advice;
    }

    public abstract void joinString(StringBuilder sb);
    
    public abstract void joinTree(StringBuilder sb, int depth);

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
