package com.site0.walnut.ext.media.edi.msg.reply.imd;

public class ImdResTransLine {

    private int lineNum;

    // Transport Line StatusType
    private String stTp;

    // Transport Line StatusDesc
    private String stDesc;

    public ImdResTransLine() {
    }

    public ImdResTransLine(int lineNum, String stTp, String stDesc) {
        this.lineNum = lineNum;
        this.stTp = stTp;
        this.stDesc = stDesc;
    }


    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public String getStTp() {
        return stTp;
    }

    public void setStTp(String stTp) {
        this.stTp = stTp;
    }

    public String getStDesc() {
        return stDesc;
    }

    public void setStDesc(String stDesc) {
        this.stDesc = stDesc;
    }
}
