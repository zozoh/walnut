package org.nutz.walnut.ext.sys.sql;

import org.nutz.lang.Strings;

public class WnDaoConfig {

    private WnDaoAuth auth;

    private String dao;

    private String tableName;

    public WnDaoAuth getAuth() {
        return auth;
    }

    public void setAuth(WnDaoAuth info) {
        this.auth = info;
    }

    public String getDaoName() {
        return Strings.sBlank(dao, "default");
    }

    public String getDao() {
        return dao;
    }

    public void setDao(String dao) {
        this.dao = dao;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String toString() {
        return String.format("%s:%s:%s", dao, tableName);
    }

}
