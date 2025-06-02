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


    public IcsReplyUbmRes() {
        super("UBMREQR");
        inTrans = new HashMap<>();
        mainTrans = new HashMap<>();
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
}
