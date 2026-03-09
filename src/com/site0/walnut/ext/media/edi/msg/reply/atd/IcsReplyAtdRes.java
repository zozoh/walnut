package com.site0.walnut.ext.media.edi.msg.reply.atd;

import com.site0.walnut.ext.media.edi.msg.reply.IcsCommonReply;

import java.util.Map;

public class IcsReplyAtdRes extends IcsCommonReply {

    // --- DTM 相关 ---
    // Clearance date, format is YYYYMMDD
    private String clearDate;

    // Payment date, format is YYYYMMDD
    private String paymentDate;

    // Collection start date (DTM+234), format is YYYYMMDD
    private String collectStartDate;

    // Collection end date (DTM+235), format is YYYYMMDD
    private String collectEndDate;

    // --- FTX 相关 ---
    // AHN
    private Map<String, String> atdStatus;

    // CIP
    private String actionReason;

    // Transport marks and numbers (FTX+MKS)
    private String transMarksNums;

    // --- TDT 相关 ---
    private String voyageNum;
    private String transportMode;
    private String airlineCode;
    private String vesselId;

    // --- LOC 相关 ---
    private String dischargePort;
    private String warehouseEstId;

    // --- GIS 相关 ---
    private String actionIndicator;

    // --- NAD 相关 ---
    private String msgRecipient;
    private String branchId;
    private String brokerLicNum;
    private String brokerName;
    private String borkerBoxNum;
    // Customs officer state (NAD+CM, component 1)
    private String customsState;
    private String workgroupName;
    private String customsOfficer;
    private String importerName;
    private String importerBoxNum;

    // --- SG2 COM 相关 ---
    private String customsEmail;
    private String customsFaxNum;
    private String customsTel;

    // --- RFF 相关 ---
    private String natureType; // AAE


//    private String senderRef; // ABO
//    private String senderRefVer;

    private String importerRef; // ABQ
    private String cusDecNum; // ABT
    private String cusDecVer;
    private String brokerRef; // ADU
    // Authority To Deal security code (RFF+AIA)
    private String dealSecurityCode;
    // Customs receipt for goods identifier (RFF+REN)
    private String goodsReceiptId;

    // --- CNT 相关 ---
//    private Map<String, Integer> controlTotals;

    private Integer cntLine;

    private Integer cntLineAndSubItem;

    private Integer cntCusDetailLine;

    private Integer cntTotalPkg;

    private Integer cntGoodsItem;

    public IcsReplyAtdRes() {
        super("ATD");
    }

    public String getClearDate() {
        return clearDate;
    }

    public void setClearDate(String clearDate) {
        this.clearDate = clearDate;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getCollectStartDate() {
        return collectStartDate;
    }

    public void setCollectStartDate(String collectStartDate) {
        this.collectStartDate = collectStartDate;
    }

    public String getCollectEndDate() {
        return collectEndDate;
    }

    public void setCollectEndDate(String collectEndDate) {
        this.collectEndDate = collectEndDate;
    }

    public Map<String, String> getAtdStatus() {
        return atdStatus;
    }

    public void setAtdStatus(Map<String, String> atdStatus) {
        this.atdStatus = atdStatus;
    }

    public String getActionReason() {
        return actionReason;
    }

    public void setActionReason(String actionReason) {
        this.actionReason = actionReason;
    }

    public String getTransMarksNums() {
        return transMarksNums;
    }

    public void setTransMarksNums(String transMarksNums) {
        this.transMarksNums = transMarksNums;
    }

    public String getVoyageNum() {
        return voyageNum;
    }

    public void setVoyageNum(String voyageNum) {
        this.voyageNum = voyageNum;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }

    public String getAirlineCode() {
        return airlineCode;
    }

    public void setAirlineCode(String airlineCode) {
        this.airlineCode = airlineCode;
    }

    public String getVesselId() {
        return vesselId;
    }

    public void setVesselId(String vesselId) {
        this.vesselId = vesselId;
    }

    public String getDischargePort() {
        return dischargePort;
    }

    public void setDischargePort(String dischargePort) {
        this.dischargePort = dischargePort;
    }

    public String getWarehouseEstId() {
        return warehouseEstId;
    }

    public void setWarehouseEstId(String warehouseEstId) {
        this.warehouseEstId = warehouseEstId;
    }

    public String getActionIndicator() {
        return actionIndicator;
    }

    public void setActionIndicator(String actionIndicator) {
        this.actionIndicator = actionIndicator;
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

    public String getCustomsState() {
        return customsState;
    }

    public void setCustomsState(String customsState) {
        this.customsState = customsState;
    }

    public String getWorkgroupName() {
        return workgroupName;
    }

    public void setWorkgroupName(String workgroupName) {
        this.workgroupName = workgroupName;
    }

    public String getCustomsOfficer() {
        return customsOfficer;
    }

    public void setCustomsOfficer(String customsOfficer) {
        this.customsOfficer = customsOfficer;
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

    public String getCustomsEmail() {
        return customsEmail;
    }

    public void setCustomsEmail(String customsEmail) {
        this.customsEmail = customsEmail;
    }

    public String getCustomsFaxNum() {
        return customsFaxNum;
    }

    public void setCustomsFaxNum(String customsFaxNum) {
        this.customsFaxNum = customsFaxNum;
    }

    public String getCustomsTel() {
        return customsTel;
    }

    public void setCustomsTel(String customsTel) {
        this.customsTel = customsTel;
    }

    public String getNatureType() {
        return natureType;
    }

    public void setNatureType(String natureType) {
        this.natureType = natureType;
    }

    public String getImporterRef() {
        return importerRef;
    }

    public void setImporterRef(String importerRef) {
        this.importerRef = importerRef;
    }

    public String getCusDecNum() {
        return cusDecNum;
    }

    public void setCusDecNum(String cusDecNum) {
        this.cusDecNum = cusDecNum;
    }

    public String getCusDecVer() {
        return cusDecVer;
    }

    public void setCusDecVer(String cusDecVer) {
        this.cusDecVer = cusDecVer;
    }

    public String getBrokerRef() {
        return brokerRef;
    }

    public void setBrokerRef(String brokerRef) {
        this.brokerRef = brokerRef;
    }

    public String getDealSecurityCode() {
        return dealSecurityCode;
    }

    public void setDealSecurityCode(String dealSecurityCode) {
        this.dealSecurityCode = dealSecurityCode;
    }

    public String getGoodsReceiptId() {
        return goodsReceiptId;
    }

    public void setGoodsReceiptId(String goodsReceiptId) {
        this.goodsReceiptId = goodsReceiptId;
    }

//    public Map<String, Integer> getControlTotals() {
//        return controlTotals;
//    }
//
//    public void setControlTotals(Map<String, Integer> controlTotals) {
//        this.controlTotals = controlTotals;
//    }


    public Integer getCntLine() {
        return cntLine;
    }

    public void setCntLine(Integer cntLine) {
        this.cntLine = cntLine;
    }

    public Integer getCntLineAndSubItem() {
        return cntLineAndSubItem;
    }

    public void setCntLineAndSubItem(Integer cntLineAndSubItem) {
        this.cntLineAndSubItem = cntLineAndSubItem;
    }

    public Integer getCntCusDetailLine() {
        return cntCusDetailLine;
    }

    public void setCntCusDetailLine(Integer cntCusDetailLine) {
        this.cntCusDetailLine = cntCusDetailLine;
    }

    public Integer getCntTotalPkg() {
        return cntTotalPkg;
    }

    public void setCntTotalPkg(Integer cntTotalPkg) {
        this.cntTotalPkg = cntTotalPkg;
    }

    public Integer getCntGoodsItem() {
        return cntGoodsItem;
    }

    public void setCntGoodsItem(Integer cntGoodsItem) {
        this.cntGoodsItem = cntGoodsItem;
    }
}
