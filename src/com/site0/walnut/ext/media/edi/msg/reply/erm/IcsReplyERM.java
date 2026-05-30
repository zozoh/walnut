package com.site0.walnut.ext.media.edi.msg.reply.erm;

import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyError;
import com.site0.walnut.ext.media.edi.msg.reply.IcsCommonReply;

public class IcsReplyERM extends IcsCommonReply {

    public static final String ERM_TYPE_ACR = "ACR";
    public static final String ERM_TYPE_SCR = "SCR";

    /**
     * 是否为 ERM 错误响应报文
     */
    private boolean ermError;

    /**
     * ERM 类型: ACR / SCR
     */
    private String ermType;

    /**
     * ICS Site Id, 来自 NAD+MR
     */
    private String icsSiteId;

    /**
     * 报文接收时间, 来自 DTM+310
     */
    private String msgRcvTime;

    /**
     * 具体错误信息
     */
    private EdiReplyError[] errs;

    /**
     * 错误条目数量
     */
    private int errCount;

    public IcsReplyERM() {
        super("ERM");
    }

    public boolean isErmError() {
        return ermError;
    }

    public void setErmError(boolean ermError) {
        this.ermError = ermError;
    }

    public String getErmType() {
        return ermType;
    }

    public void setErmType(String ermType) {
        this.ermType = ermType;
    }

    public String getIcsSiteId() {
        return icsSiteId;
    }

    public void setIcsSiteId(String icsSiteId) {
        this.icsSiteId = icsSiteId;
    }

    public String getMsgRcvTime() {
        return msgRcvTime;
    }

    public void setMsgRcvTime(String msgRcvTime) {
        this.msgRcvTime = msgRcvTime;
    }

    public EdiReplyError[] getErrs() {
        return errs;
    }

    public void setErrs(EdiReplyError[] errs) {
        this.errs = errs;
    }

    public int getErrCount() {
        return errCount;
    }

    public void setErrCount(int errCount) {
        this.errCount = errCount;
    }

    public boolean isAcrType() {
        return ERM_TYPE_ACR.equalsIgnoreCase(ermType);
    }

    public boolean isScrType() {
        return ERM_TYPE_SCR.equalsIgnoreCase(ermType);
    }
}

