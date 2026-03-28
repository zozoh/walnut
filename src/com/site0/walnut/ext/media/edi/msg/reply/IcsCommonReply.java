package com.site0.walnut.ext.media.edi.msg.reply;

public abstract class IcsCommonReply {

    // 返回消息解析结果的结构版本, 默认是0, 若后续有修改, 则会增加版本号，用于区分新老结构
    private int rstVer = 0;

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


    // BGM 报文行
    private int funcCode; // 11: Response, 8: Status, 32: Approval
    private String docName;

    // UNT 报文行
//    private int segNum;
//    private int msgIdx;

    public IcsCommonReply(String msgType) {
        this.msgType = msgType;
        this.refVer = -1;
        this.funcCode = -1;
    }

    public int getRstVer() {
        return rstVer;
    }

    public void setRstVer(int rstVer) {
        this.rstVer = rstVer;
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

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

//    public int getSegNum() {
//        return segNum;
//    }
//
//    public void setSegNum(int segNum) {
//        this.segNum = segNum;
//    }
//
//    public int getMsgIdx() {
//        return msgIdx;
//    }
//
//    public void setMsgIdx(int msgIdx) {
//        this.msgIdx = msgIdx;
//    }
}
