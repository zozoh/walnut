package com.site0.walnut.ext.media.edi.msg.reply.cargorpt;

import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyError;
import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyObj;

import java.util.Map;

/**
 * AIR CARGO REPORT 的返回报文对象
 *
 * @author jrrx
 */
public class EdiReplyAIRCRR extends EdiReplyObj {

    public EdiReplyAIRCRR() {
        super("AIRCRR");
    }

    /**
     * (1) ERC信息类型有: ERROR, WARN, ADVICE;
     * (2) 若 errList 中存在 WARN和 ERROR，则 success = false，反之则 success = true。
     */
    private boolean success;

    /**
     * 消息发送记录的 ID ，一般就是发件箱发送对象的 id。
     * 对应 RFF+ABO 报文行
     */
    private String referId;

    /**
     * referId 的小写形式
     */
    private String referIdInLower;

    /**
     * referId 的 version 版本号
     */
    private int refVer;

    /**
     * 如果错误，这里是具体的错误信息
     */
    private EdiReplyError[] errors;

    /**
     * (1) 存放 NAD 相关信息;
     * (2) 存放 RFF 相关信息;
     * (3) 存放 DTM 相关信息。
     */
    private Map<String, Object> extraInfo;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getReferId() {
        return referId;
    }

    public void setReferId(String referId) {
        this.referId = referId;
    }

    public String getReferIdInLower() {
        return referIdInLower;
    }

    public void setReferIdInLower(String referIdInLower) {
        this.referIdInLower = referIdInLower;
    }

    public int getRefVer() {
        return refVer;
    }

    public void setRefVer(int refVer) {
        this.refVer = refVer;
    }

    public EdiReplyError[] getErrors() {
        return errors;
    }

    public void setErrors(EdiReplyError[] errors) {
        this.errors = errors;
    }

    public Map<String, Object> getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(Map<String, Object> extraInfo) {
        this.extraInfo = extraInfo;
    }
}
