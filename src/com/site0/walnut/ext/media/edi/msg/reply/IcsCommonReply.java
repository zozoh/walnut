package com.site0.walnut.ext.media.edi.msg.reply;

public abstract class IcsCommonReply {

    // 返回的消息类型
    protected String msgType;

    protected boolean success;

    /**
     * 消息发送记录的 ID ，一般就是发件箱发送对象的 id。
     */
    private String refId;

    /**
     * referId 的小写形式
     */
    private String refIdInLower;

    private int refVer;

    /**
     * 11: Response, 8: Status, 32: Approval
     */
    private int funcCode;

    public IcsCommonReply(String msgType) {
        this.msgType = msgType;
        this.refVer = -1;
        this.funcCode = -1;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
        this.refIdInLower = (null == refId ? null : refId.toLowerCase());
    }

    public String getRefIdInLower() {
        return refIdInLower;
    }

    public void setRefIdInLower(String refIdInLower) {
        this.refIdInLower = refIdInLower;
    }

    public int getRefVer() {
        return refVer;
    }

    public void setRefVer(int refVer) {
        this.refVer = refVer;
    }

    public int getFuncCode() {
        return funcCode;
    }

    public void setFuncCode(int funcCode) {
        this.funcCode = funcCode;
    }
}
