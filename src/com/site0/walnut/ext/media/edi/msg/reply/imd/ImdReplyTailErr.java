package com.site0.walnut.ext.media.edi.msg.reply.imd;


import com.site0.walnut.ext.media.edi.bean.EdiSegment;

import java.util.List;

/**
 * TailError segment example
 * --------------------------------------------
 * ERP+::402
 * ERC+1::95
 * ERC+2::95
 * ERC+3::95
 * ERC+4::95
 * ERC+5::95
 * FTX+ABS+++TO CLAIM PREFERENTIAL ...
 * --------------------------------------------
 */

public class ImdReplyTailErr {

    private String advId;

    private List<String> advLoc;

    private List<String> advDesc;

    public ImdReplyTailErr() {}

    public ImdReplyTailErr(List<EdiSegment> segs) {
        // todo
    }

    public String getAdvId() {
        return advId;
    }

    public void setAdvId(String advId) {
        this.advId = advId;
    }

    public List<String> getAdvLoc() {
        return advLoc;
    }

    public void setAdvLoc(List<String> advLoc) {
        this.advLoc = advLoc;
    }

    public List<String> getAdvDesc() {
        return advDesc;
    }

    public void setAdvDesc(List<String> advDesc) {
        this.advDesc = advDesc;
    }
}
