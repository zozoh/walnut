package com.site0.walnut.ext.media.edi.msg.reply.ubm;

import com.site0.walnut.ext.media.edi.msg.reply.IcsCommonReply;

import java.util.HashMap;
import java.util.Map;

public class IcsReplyUbmRes extends IcsCommonReply {

    // Processing date/time
    private String txnTime;

    private String cargoMsgId;

    private String mvTypeText;

    private Map<String, String> inTrans;

    private Map<String, String> mainTrans;

    private Map<String, String> locInfo;

    // RequestReason
    private String reqReason;

    // 转运编号
    private String transShipNum;

    // 转移请求的结果: "Underbond Approval" 或 "Underbond Approval Rescind Notice"
    private String ubmNotice;


    public IcsReplyUbmRes() {
        super("UBMREQR");
        inTrans = new HashMap<>();
        mainTrans = new HashMap<>();
        locInfo = new HashMap<>();
    }

    public String getTxnTime() {
        return txnTime;
    }

    public void setTxnTime(String txnTime) {
        this.txnTime = txnTime;
    }

    public String getCargoMsgId() {
        return cargoMsgId;
    }

    public void setCargoMsgId(String cargoMsgId) {
        this.cargoMsgId = cargoMsgId;
    }

    public String getMvTypeText() {
        return mvTypeText;
    }

    public void setMvTypeText(String mvTypeText) {
        this.mvTypeText = mvTypeText;
    }

    public Map<String, String> getInTrans() {
        return inTrans;
    }

    public void setInTrans(Map<String, String> inTrans) {
        this.inTrans = inTrans;
    }

    public Map<String, String> getMainTrans() {
        return mainTrans;
    }

    public void setMainTrans(Map<String, String> mainTrans) {
        this.mainTrans = mainTrans;
    }

    public Map<String, String> getLocInfo() {
        return locInfo;
    }

    public void setLocInfo(Map<String, String> locInfo) {
        this.locInfo = locInfo;
    }

    public String getReqReason() {
        return reqReason;
    }

    public void setReqReason(String reqReason) {
        this.reqReason = reqReason;
    }

    public String getTransShipNum() {
        return transShipNum;
    }

    public void setTransShipNum(String transShipNum) {
        this.transShipNum = transShipNum;
    }

    public String getUbmNotice() {
        return ubmNotice;
    }

    public void setUbmNotice(String ubmNotice) {
        this.ubmNotice = ubmNotice;
    }
}
