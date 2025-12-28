package com.site0.walnut.ext.media.edi.msg.reply.imd;

import java.util.List;
import java.util.Map;

public class ImdResEntryLine {

    private int lineNum;

    private String natureType;

    private String dutyRateDesc;

    private List<Map<String, String>> dutyRates;

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public String getNatureType() {
        return natureType;
    }

    public void setNatureType(String natureType) {
        this.natureType = natureType;
    }

    public String getDutyRateDesc() {
        return dutyRateDesc;
    }

    public void setDutyRateDesc(String dutyRateDesc) {
        this.dutyRateDesc = dutyRateDesc;
    }

    public List<Map<String, String>> getDutyRates() {
        return dutyRates;
    }

    public void setDutyRates(List<Map<String, String>> dutyRates) {
        this.dutyRates = dutyRates;
    }
}
