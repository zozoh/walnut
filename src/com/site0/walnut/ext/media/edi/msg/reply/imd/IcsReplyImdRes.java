package com.site0.walnut.ext.media.edi.msg.reply.imd;

import com.site0.walnut.ext.media.edi.msg.reply.IcsCommonReply;

import java.util.HashMap;
import java.util.Map;

public class IcsReplyImdRes extends IcsCommonReply {

    //Effective Duty Date, format is YYYYMMDD
    private String dutyDate;

    private Map<String, String> imdStatus;

    private boolean preLodge;

    // Line Liabilities Breakdown Indicator
    private boolean llb;

    // Total Liabilities Breakdown Indicator
    private boolean tlb;


    public IcsReplyImdRes() {
        super("CUSRES");
        imdStatus = new HashMap<>();
    }

    public String getDutyDate() {
        return dutyDate;
    }

    public void setDutyDate(String dutyDate) {
        this.dutyDate = dutyDate;
    }

    public Map<String, String> getImdStatus() {
        return imdStatus;
    }

    public void setImdStatus(Map<String, String> imdStatus) {
        this.imdStatus = imdStatus;
    }

    public boolean isPreLodge() {
        return preLodge;
    }

    public void setPreLodge(boolean preLodge) {
        this.preLodge = preLodge;
    }

    public boolean isLlb() {
        return llb;
    }

    public void setLlb(boolean llb) {
        this.llb = llb;
    }

    public boolean isTlb() {
        return tlb;
    }

    public void setTlb(boolean tlb) {
        this.tlb = tlb;
    }
}
