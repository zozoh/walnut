package com.site0.walnut.ext.media.edi.msg.reply.imd;

import java.util.List;

public class ImdResTransLine {

    private int lineNum;

    // Transport Line StatusType
    private String stTp;

    // Transport Line StatusDesc
    private String stDesc;


    // imd entry lines
    private List<ImdResEntryLine> entryLines;

    /**
     * --- Segment Group 13: ERP-ERC-FTX ---
     * this is tail advice
     */
    private List<ImdReplyLineErr> lineErrs;


    public ImdResTransLine() {
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

    public List<ImdResEntryLine> getEntryLines() {
        return entryLines;
    }

    public void setEntryLines(List<ImdResEntryLine> entryLines) {
        this.entryLines = entryLines;
    }

    public List<ImdReplyLineErr> getLineErrs() {
        return lineErrs;
    }

    public void setLineErrs(List<ImdReplyLineErr> lineErrs) {
        this.lineErrs = lineErrs;
    }
}
