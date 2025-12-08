package com.site0.walnut.ext.media.edi.msg.reply.imd;

import com.site0.walnut.ext.media.edi.msg.reply.IcsCommonReply;

import java.util.HashMap;
import java.util.Map;

public class IcsReplyImdRes extends IcsCommonReply {

    //Effective Duty Date, format is YYYYMMDD
    private String dutyDate;

    private Map<String, String> imdStatus;

    // --- GIS 相关 ---
    private boolean preLodge;
    private boolean llb; // Line Liabilities Breakdown Indicator
    private boolean tlb;// Total Liabilities Breakdown Indicator

    // --- NAD 相关---
    private String msgRecipient;
    private String branchId;
    private String brokerLicNum;
    private String brokerName;
    private String borkerBoxNum;
    private String importerName;
    private String importerBoxNum;

    // --- RFF 相关 ---
    private String headerNatureType; // AAE
    private String importerRef; // ABQ
    private String impDecNum; // ABT
    private String impDecVer;
    private String brokerRef; // ADU


    public IcsReplyImdRes() {
        super("CUSRES");
        imdStatus = new HashMap<>();
    }

    public String getDutyDate() {
        return dutyDate;
    }

    public void setDutyDate(String dutyDate) {
        this.dutyDate = dutyDate;
    }

    public Map<String, String> getImdStatus() {
        return imdStatus;
    }

    public void setImdStatus(Map<String, String> imdStatus) {
        this.imdStatus = imdStatus;
    }

    public boolean isPreLodge() {
        return preLodge;
    }

    public void setPreLodge(boolean preLodge) {
        this.preLodge = preLodge;
    }

    public boolean isLlb() {
        return llb;
    }

    public void setLlb(boolean llb) {
        this.llb = llb;
    }

    public boolean isTlb() {
        return tlb;
    }

    public void setTlb(boolean tlb) {
        this.tlb = tlb;
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

    public String getHeaderNatureType() {
        return headerNatureType;
    }

    public void setHeaderNatureType(String headerNatureType) {
        this.headerNatureType = headerNatureType;
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
}
