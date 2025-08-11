package com.site0.walnut.ext.data.sqlx.hislog;

import com.site0.walnut.util.Ws;

public class SqlxHisTarget {

    private String from;

    private String dao;

    private String sqlInsert;

    public boolean isValid() {
        return !Ws.isBlank(from) && !Ws.isBlank(sqlInsert);
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getDao() {
        return dao;
    }

    public void setDao(String dao) {
        this.dao = dao;
    }

    public String getSqlInsert() {
        return sqlInsert;
    }

    public void setSqlInsert(String sqlInsert) {
        this.sqlInsert = sqlInsert;
    }

}
