package com.site0.walnut.ext.media.edi.msg.reply.imd;


import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import org.nutz.lang.util.NutBean;

import java.util.ArrayList;
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

public class ImdReplyLineErr {

    private String advId;

    private List<String> advLoc;

    private List<String> advDesc;

    public ImdReplyLineErr() {
    }

    public ImdReplyLineErr(List<EdiSegment> segs) {
        this.advLoc = new ArrayList<>();
        this.advDesc = new ArrayList<>();
        for (EdiSegment seg : segs) {
            if (seg.is("ERP")) {
                // ERP+::{MessageAdviceIdentifier}'
                NutBean bean = seg.getBean(null, ",,advId");
                this.advId = bean.getString("advId");
            } else if (seg.is("ERC")) {
                // ERC+1::95'
                NutBean bean = seg.getBean(null, "advLoc,,");
                this.advLoc.add(bean.getString("advLoc"));
            } else if (seg.is("FTX")) {
                // FTX+ABS+++{MessageAdviceDesc}'
                NutBean bean = seg.getBean(null, null, null, null, "advDesc");
                this.advDesc.add(bean.getString("advDesc"));
            }
        }
        if (this.advLoc.size() == 0) {
            this.advLoc = null;
        }
        if (this.advDesc.size() == 0) {
            this.advDesc = null;
        }
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
