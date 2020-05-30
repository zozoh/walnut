package org.nutz.walnut.ext.sql;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

public class WnDaoConfig {

    private String dao;

    private String tableName;

    private NutMap setup;

    private WnDaoConnectionInfo connectionInfo;

    public String getDao() {
        return dao;
    }

    public String getDaoName() {
        return Strings.sBlank(dao, "default");
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

    public NutMap getSetup() {
        return setup;
    }

    public WnDaoConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(WnDaoConnectionInfo info) {
        this.connectionInfo = info;
    }

    public void setSetup(NutMap setup) {
        this.setup = setup;
    }

    public String toString() {
        return String.format("%s:%s:%s", dao, tableName, Json.toJson(setup));
    }

}
