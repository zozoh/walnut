package com.site0.walnut.ext.data.sqlx.hislog;

import org.nutz.lang.util.NutMap;

public class SqlxHislogConfigItem {

    private String sqlName;

    private Object test;

    private NutMap data;

    private String to;

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

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

}
