package com.site0.walnut.ext.media.edi.msg.reply;

import java.util.HashMap;
import java.util.Map;

public class CargoStaAdviceObj extends EdiReplyObj {

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

    private Map<String, String> statusMap;

    private Map<String, String> infoMap;

    private Map<String, String> refMap;


    public CargoStaAdviceObj() {
        super("CARST");
        statusMap = new HashMap<>();
        infoMap = new HashMap<>();
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

    public Map<String, String> getStatusMap() {
        return statusMap;
    }

    public void setStatusMap(Map<String, String> statusMap) {
        this.statusMap = statusMap;
    }

    public Map<String, String> getInfoMap() {
        return infoMap;
    }

    public void setInfoMap(Map<String, String> infoMap) {
        this.infoMap = infoMap;
    }

    public Map<String, String> getRefMap() {
        return refMap;
    }

    public void setRefMap(Map<String, String> refMap) {
        this.refMap = refMap;
    }
}
