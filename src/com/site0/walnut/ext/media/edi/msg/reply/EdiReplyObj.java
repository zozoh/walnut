package com.site0.walnut.ext.media.edi.msg.reply;

public abstract class EdiReplyObj {

    protected String msgType;

    public EdiReplyObj(String replyType) {
        this.msgType = replyType;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String replyType) {
        this.msgType = replyType;
    }

}
