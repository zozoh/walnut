package com.site0.walnut.ext.media.edi.msg.reply;

import com.site0.walnut.ext.media.edi.msg.reply.contrl.CntrIcRes;
import com.site0.walnut.ext.media.edi.msg.reply.contrl.CntrMsgRes;

import java.util.List;

/**
 * 控制信息的返回
 *
 * @author zozoh(zozohtnt @ gmail.com)
 */
public class EdiReplyCONTRL {

    private String msgType;

    private CntrIcRes ic;

    private List<CntrMsgRes> msgs;

    public EdiReplyCONTRL() {
        this.msgType = "CONTRL";
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public CntrIcRes getIc() {
        return ic;
    }

    public void setIc(CntrIcRes ic) {
        this.ic = ic;
    }

    public List<CntrMsgRes> getMsgs() {
        return msgs;
    }

    public void setMsgs(List<CntrMsgRes> msgs) {
        this.msgs = msgs;
    }
}
