package com.site0.walnut.ext.media.edi.msg.reply.pay;

import com.site0.walnut.ext.media.edi.msg.reply.IcsCommonReply;

public class RefundRej extends IcsCommonReply {

    public RefundRej() {
        super("REFREJ");
    }

    // FTX+ACB 报文行
    private String clientRef;
    // FTX+ACD 报文行
    private String cusActReason;
    private String rejectReason;

    // NAD+MR 报文行
    private String msgRecipient;
    private String clientName;

    // NAD+VT 报文行
    private String branchId;

    // NAD+CB 报文行
    private String brokerLicNum;
    private String brokerName;
    private String brokerBoxNum;

    // NAD+CM 报文行
    private String cusState;
    private String cusGroup;
    private String cusName;

    // COM 报文行
    private String cusEmail;
    private String cusFax;
    private String cusTel;

    // RFF+ABO 报文行
    private String refId;        // ABO
    private String refIdInLower;
    private int refVer;

    // RFF+ABT
    private String impDecNum;
    private String impDecNumVer;

    // RFF+ADU
    private String brokerRef;

    public String getClientRef() {
        return clientRef;
    }

    public void setClientRef(String clientRef) {
        this.clientRef = clientRef;
    }

    public String getCusActReason() {
        return cusActReason;
    }

    public void setCusActReason(String cusActReason) {
        this.cusActReason = cusActReason;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getMsgRecipient() {
        return msgRecipient;
    }

    public void setMsgRecipient(String msgRecipient) {
        this.msgRecipient = msgRecipient;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getBrokerLicNum() {
        return brokerLicNum;
    }

    public void setBrokerLicNum(String brokerLicNum) {
        this.brokerLicNum = brokerLicNum;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public String getBrokerBoxNum() {
        return brokerBoxNum;
    }

    public void setBrokerBoxNum(String brokerBoxNum) {
        this.brokerBoxNum = brokerBoxNum;
    }

    public String getCusState() {
        return cusState;
    }

    public void setCusState(String cusState) {
        this.cusState = cusState;
    }

    public String getCusGroup() {
        return cusGroup;
    }

    public void setCusGroup(String cusGroup) {
        this.cusGroup = cusGroup;
    }

    public String getCusName() {
        return cusName;
    }

    public void setCusName(String cusName) {
        this.cusName = cusName;
    }

    public String getCusEmail() {
        return cusEmail;
    }

    public void setCusEmail(String cusEmail) {
        this.cusEmail = cusEmail;
    }

    public String getCusFax() {
        return cusFax;
    }

    public void setCusFax(String cusFax) {
        this.cusFax = cusFax;
    }

    public String getCusTel() {
        return cusTel;
    }

    public void setCusTel(String cusTel) {
        this.cusTel = cusTel;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
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

    public String getImpDecNum() {
        return impDecNum;
    }

    public void setImpDecNum(String impDecNum) {
        this.impDecNum = impDecNum;
    }

    public String getImpDecNumVer() {
        return impDecNumVer;
    }

    public void setImpDecNumVer(String impDecNumVer) {
        this.impDecNumVer = impDecNumVer;
    }

    public String getBrokerRef() {
        return brokerRef;
    }

    public void setBrokerRef(String brokerRef) {
        this.brokerRef = brokerRef;
    }


}
