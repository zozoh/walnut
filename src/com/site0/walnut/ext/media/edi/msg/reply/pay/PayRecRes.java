package com.site0.walnut.ext.media.edi.msg.reply.pay;

import com.site0.walnut.ext.media.edi.msg.reply.IcsCommonReply;

import java.util.List;
import java.util.Map;

public class PayRecRes extends IcsCommonReply {
    
    private String paymentDate;

    private String msgRecipient;

    private String bankAccName;

    private String bankAccNum;

    private String abn;

    private String bsbNum;

    private String importerId;

    private String branchId;

    private String importerRef;

    private String impDecNum;

    private String brokerRef;

    private String bankTransNum;

    private String bankReceiptNum;

    List<Map<String, String>> moaList;

    public PayRecRes() {
        super("PAYREC");
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getMsgRecipient() {
        return msgRecipient;
    }

    public void setMsgRecipient(String msgRecipient) {
        this.msgRecipient = msgRecipient;
    }

    public String getBankAccName() {
        return bankAccName;
    }

    public void setBankAccName(String bankAccName) {
        this.bankAccName = bankAccName;
    }

    public String getBankAccNum() {
        return bankAccNum;
    }

    public void setBankAccNum(String bankAccNum) {
        this.bankAccNum = bankAccNum;
    }

    public String getAbn() {
        return abn;
    }

    public void setAbn(String abn) {
        this.abn = abn;
    }

    public String getBsbNum() {
        return bsbNum;
    }

    public void setBsbNum(String bsbNum) {
        this.bsbNum = bsbNum;
    }

    public String getImporterId() {
        return importerId;
    }

    public void setImporterId(String importerId) {
        this.importerId = importerId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
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

    public String getBrokerRef() {
        return brokerRef;
    }

    public void setBrokerRef(String brokerRef) {
        this.brokerRef = brokerRef;
    }

    public String getBankTransNum() {
        return bankTransNum;
    }

    public void setBankTransNum(String bankTransNum) {
        this.bankTransNum = bankTransNum;
    }

    public String getBankReceiptNum() {
        return bankReceiptNum;
    }

    public void setBankReceiptNum(String bankReceiptNum) {
        this.bankReceiptNum = bankReceiptNum;
    }

    public List<Map<String, String>> getMoaList() {
        return moaList;
    }

    public void setMoaList(List<Map<String, String>> moaList) {
        this.moaList = moaList;
    }
}