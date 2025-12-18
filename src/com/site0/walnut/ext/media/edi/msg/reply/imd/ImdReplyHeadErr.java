package com.site0.walnut.ext.media.edi.msg.reply.imd;


import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.util.Ws;
import org.nutz.lang.util.NutBean;

import java.util.ArrayList;
import java.util.List;

/**
 * HeadError segment example
 * --------------------------------------------
 * ERP+::1
 * ERC+ID0302::95
 * FTX+AAO+++COMMUNITY PROTECTION PERMIT IS APPLICABLE AND REQUIRED
 * --------------------------------------------
 */
public class ImdReplyHeadErr {

    // 报文模板: ERP+::{ErrLocation}'
    private String errLoc;

    // 报文模板: ERC+{ErrId}::95'
    private List<String> errId;

    // 报文模板: FTX+AAO+++{ErrDesc}'
    private List<String> errDesc;

    public ImdReplyHeadErr() {
    }

    public ImdReplyHeadErr(List<EdiSegment> segs) {
        for (EdiSegment seg : segs) {
            if (seg.is("ERP")) {
                NutBean bean = seg.getBean(null, ",,errLocation");
                this.errLoc = bean.getString("errLocation");
            } else if (seg.is("ERC")) {
                NutBean bean = seg.getBean(null, "errId,,");
                String errIdStr = bean.getString("errId");
                if (!Ws.isBlank(errIdStr)) {
                    if (this.errId == null) {
                        this.errId = new ArrayList<>();
                    }
                    this.errId.add(errIdStr);
                }
            } else if (seg.is("FTX")) {
                NutBean bean = seg.getBean(null, null, null, null, "errDesc");
                String errDescStr = bean.getString("errDesc");
                if (!Ws.isBlank(errDescStr)) {
                    if (this.errDesc == null) {
                        this.errDesc = new ArrayList<>();
                    }
                    this.errDesc.add(errDescStr);
                }
            }
        }
    }

    public String getErrLoc() {
        return errLoc;
    }

    public void setErrLoc(String errLoc) {
        this.errLoc = errLoc;
    }

    public List<String> getErrId() {
        return errId;
    }

    public void setErrId(List<String> errId) {
        this.errId = errId;
    }

    public List<String> getErrDesc() {
        return errDesc;
    }

    public void setErrDesc(List<String> errDesc) {
        this.errDesc = errDesc;
    }
}
