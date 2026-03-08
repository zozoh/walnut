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

    public static List<ImdReplyHeadErr> valueOf(List<EdiSegment> segs) {
        List<ImdReplyHeadErr> replyHeadErrList = new ArrayList<>();
        ImdReplyHeadErr headErr = null;
        for (EdiSegment seg : segs) {
            if (seg.is("ERP")) {
                headErr = new ImdReplyHeadErr();
                replyHeadErrList.add(headErr);
                NutBean bean = seg.getBean(null, ",,errLocation");
                headErr.setErrLoc(bean.getString("errLocation"));
            } else if (seg.is("ERC")) {
                if (headErr == null) {
                    continue;
                }
                NutBean bean = seg.getBean(null, "errId,,");
                String errIdStr = bean.getString("errId");
                if (!Ws.isBlank(errIdStr)) {
                    if (headErr.getErrId() == null) {
                        headErr.setErrId(new ArrayList<>());
                    }
                    headErr.getErrId().add(errIdStr);
                }
            } else if (seg.is("FTX")) {
                NutBean bean = seg.getBean(null, null, null, null, "errDesc");
                String errDescStr = bean.getString("errDesc");
                if (!Ws.isBlank(errDescStr)) {
                    if (headErr.getErrDesc() == null){
                        headErr.setErrDesc(new ArrayList<>());
                    }
                    headErr.getErrDesc().add(errDescStr);
                }
            }
        }
        return replyHeadErrList;
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
