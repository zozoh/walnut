package com.site0.walnut.ext.media.edi.msg.reply.sam;

import com.site0.walnut.ext.media.edi.msg.reply.IcsCommonReply;

import java.util.ArrayList;
import java.util.Map;

public class IcsReplySamRes extends IcsCommonReply {

    public IcsReplySamRes() {
        super("SAM");
    }

    private Map<String, String> imdStatus;


    // --- NAD 相关 ---
    private String msgRecipient;
    private String branchId;
    private String brokerLicNum;
    private String brokerName;
    private String borkerBoxNum;
    private String importerName;
    private String importerBoxNum;

    // RFF
    private String importerRef; // ABQ
    private String impDecNum; // ABT
    private String impDecVer;
    private String brokerRef; // ADU

    // advices
    private ArrayList<SamAdv> advs;

    public Map<String, String> getImdStatus() {
        return imdStatus;
    }

    public void setImdStatus(Map<String, String> imdStatus) {
        this.imdStatus = imdStatus;
    }

    public String getMsgRecipient() {
        return msgRecipient;
    }

    public void setMsgRecipient(String msgRecipient) {
        this.msgRecipient = msgRecipient;
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

    public String getBorkerBoxNum() {
        return borkerBoxNum;
    }

    public void setBorkerBoxNum(String borkerBoxNum) {
        this.borkerBoxNum = borkerBoxNum;
    }

    public String getImporterName() {
        return importerName;
    }

    public void setImporterName(String importerName) {
        this.importerName = importerName;
    }

    public String getImporterBoxNum() {
        return importerBoxNum;
    }

    public void setImporterBoxNum(String importerBoxNum) {
        this.importerBoxNum = importerBoxNum;
    }

    public String getImporterRef() {
        return importerRef;
    }

    public void setImporterRef(String importerRef) {
        this.importerRef = importerRef;
    }

    public String getImpDecNum() {
        return impDecNum;
    }

    public void setImpDecNum(String impDecNum) {
        this.impDecNum = impDecNum;
    }

    public String getImpDecVer() {
        return impDecVer;
    }

    public void setImpDecVer(String impDecVer) {
        this.impDecVer = impDecVer;
    }

    public String getBrokerRef() {
        return brokerRef;
    }

    public void setBrokerRef(String brokerRef) {
        this.brokerRef = brokerRef;
    }

    public ArrayList<SamAdv> getAdvs() {
        return advs;
    }

    public void setAdvs(ArrayList<SamAdv> advs) {
        this.advs = advs;
    }
}
