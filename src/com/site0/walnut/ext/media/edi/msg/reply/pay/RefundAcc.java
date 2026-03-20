package com.site0.walnut.ext.media.edi.msg.reply.pay;

import com.site0.walnut.ext.media.edi.msg.reply.IcsCommonReply;

public class RefundAcc extends IcsCommonReply {

    public RefundAcc() {
        super("REFACC");
    }

    // FTX+ACB
    private String drawbackId;
    // FTX+ACD
    private String cusActReason;

    // NAD
    private String msgRecipient; // MR
    private String clientName;

    private String branchId;     // VT

    private String brokerLicNum; // CB
    private String brokerName;
    private String brokerBoxNum;

    // NAD+CM
    private String cusState;
    private String cusGroup;
    private String cusName;

    private String cusEmail;
    private String cusFax;
    private String cusTel;

    // RFF
    private String refId;        // ABO
    private String refIdInLower;
    private int refVer;

    // ABT
    private String impDecNum;
    private String impDecNumVer;

    private String brokerRef;    // ADU

    public String getDrawbackId() {
        return drawbackId;
    }

    public void setDrawbackId(String drawbackId) {
        this.drawbackId = drawbackId;
    }

    public String getCusActReason() {
        return cusActReason;
    }

    public void setCusActReason(String cusActReason) {
        this.cusActReason = cusActReason;
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
