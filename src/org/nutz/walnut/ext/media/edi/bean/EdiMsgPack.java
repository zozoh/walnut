package org.nutz.walnut.ext.media.edi.bean;

import java.util.List;

public class EdiMsgPack {

    private EdiMsgAdvice advice;

    private List<EdiMsgEntry> entries;

    public EdiMsgPack valueOf(String input) {
        // 寻找第一个报文头
        int pos = input.indexOf('\n');
        if (pos > 0) {
            String una = input.substring(0, pos).trim();
            advice = new EdiMsgAdvice(una);

            // 逐个解析后面的行
            char[] cs = input.substring(pos + 1).trim().toCharArray();
        }
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
