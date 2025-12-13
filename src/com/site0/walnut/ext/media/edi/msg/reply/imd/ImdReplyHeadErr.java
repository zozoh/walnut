package com.site0.walnut.ext.media.edi.msg.reply.imd;


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
