package com.site0.walnut.ext.media.edi.msg.reply.ubm;

import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyError;
import com.site0.walnut.ext.media.edi.msg.reply.IcsCommonReply;

import java.util.LinkedHashMap;

public class IcsReplyUbmErr extends IcsCommonReply {

    /**
     * 如果错误，这里是具体的错误信息
     */
    private EdiReplyError[] errs;

    /**
     * 错误条目的数量
     */
    private int errCount;


    private LinkedHashMap<String, String> stInfo;

    private String inDocName;

    // 发送的 Request 报文的 Function Code
    private String inMsgFuncCode;

    private String msgRcvTime;

    public IcsReplyUbmErr() {
        super("UBMREQE");
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

    public LinkedHashMap<String, String> getStInfo() {
        return stInfo;
    }

    public void setStInfo(LinkedHashMap<String, String> stInfo) {
        this.stInfo = stInfo;
    }

    public String getInDocName() {
        return inDocName;
    }

    public void setInDocName(String inDocName) {
        this.inDocName = inDocName;
    }

    public String getInMsgFuncCode() {
        return inMsgFuncCode;
    }

    public void setInMsgFuncCode(String inMsgFuncCode) {
        this.inMsgFuncCode = inMsgFuncCode;
    }

    public String getMsgRcvTime() {
        return msgRcvTime;
    }

    public void setMsgRcvTime(String msgRcvTime) {
        this.msgRcvTime = msgRcvTime;
    }
}
