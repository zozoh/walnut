package com.site0.walnut.ext.data.sqlx.hislog;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.Ws;

public class HisConfigItem {

    private String sqlName;

    private Object test;

    private NutMap data;

    private HisConfigSetData[] setData;

    private String to;

    public boolean isValid() {
        return !Ws.isBlank(sqlName) && !Ws.isBlank(to) && null != data && !data.isEmpty();
    }

    public String getSqlName() {
        return sqlName;
    }

    public void setSqlName(String sqlName) {
        this.sqlName = sqlName;
    }

    public Object getTest() {
        return test;
    }

    public void setTest(Object test) {
        this.test = test;
    }

    public NutMap getData() {
        return data;
    }

    public void setData(NutMap data) {
        this.data = data;
    }

    public boolean hasSetData() {
        return null != setData && setData.length > 0;
    }

    public HisConfigSetData[] getSetData() {
        return setData;
    }

    public void setSetData(HisConfigSetData[] setData) {
        this.setData = setData;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

}
