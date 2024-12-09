package com.site0.walnut.ext.media.edi.msg.reply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IcsReplyCARST extends IcsCommonReply {
    private Map<String, String> statusMap;

    private List<String> impends;

    private Map<String, String> infoMap;

    private Map<String, String> refMap;


    public IcsReplyCARST() {
        super("CARST");
        statusMap = new HashMap<>();
        impends = new ArrayList<>();
        infoMap = new HashMap<>();
        refMap = new HashMap<>();
    }

    public Map<String, String> getStatusMap() {
        return statusMap;
    }

    public void setStatusMap(Map<String, String> statusMap) {
        this.statusMap = statusMap;
    }

    public List<String> getImpends() {
        return impends;
    }

    public void setImpends(List<String> impends) {
        this.impends = impends;
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
