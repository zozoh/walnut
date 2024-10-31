package com.site0.walnut.ext.media.edi.msg.reply;

import java.util.HashMap;
import java.util.Map;

public class CargoStaAdviceObj {

    private Map<String, String> statusMap;

    private Map<String, String> infoMap;


    public CargoStaAdviceObj() {
        statusMap = new HashMap<>();
        infoMap = new HashMap<>();
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
}
