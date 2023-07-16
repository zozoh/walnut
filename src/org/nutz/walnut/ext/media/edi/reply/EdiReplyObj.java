package org.nutz.walnut.ext.media.edi.reply;

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
