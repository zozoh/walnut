package org.nutz.walnut.ext.media.edi.bean;

import java.util.List;

public class EdiMsgPack {

    private EdiMsgAdvice advice;

    private List<EdiMsgEntry> entries;

    public EdiMsgPack valueOf(String input) {
        return this;
    }

    public EdiMsgAdvice getAdvice() {
        return advice;
    }

    public void setAdvice(EdiMsgAdvice advice) {
        this.advice = advice;
    }

    public List<EdiMsgEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<EdiMsgEntry> entries) {
        this.entries = entries;
    }

}
