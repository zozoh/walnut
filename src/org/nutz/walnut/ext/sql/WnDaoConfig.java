package org.nutz.walnut.ext.sql;

import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

public class WnDaoConfig {

    private WnDaoAuth auth;

    private String dao;

    private String tableName;

    private boolean autoCreate;

    private List<NutMap> fields;

    private String[] pks;

    private NutMap objKeys;

    private List<NutMap> indexes;

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

    public WnDaoAuth getAuth() {
        return auth;
    }

    public void setAuth(WnDaoAuth info) {
        this.auth = info;
    }

    public boolean isAutoCreate() {
        return autoCreate;
    }

    public void setAutoCreate(boolean autoCreate) {
        this.autoCreate = autoCreate;
    }

    public List<NutMap> getFields() {
        return fields;
    }

    public void setFields(List<NutMap> fields) {
        this.fields = fields;
    }

    public String[] getPks() {
        return pks;
    }

    public void setPks(String[] pks) {
        this.pks = pks;
    }

    public NutMap getObjKeys() {
        return objKeys;
    }

    public void setObjKeys(NutMap objKeys) {
        this.objKeys = objKeys;
    }

    public String getFieldName(String stdName) {
        String nm = null;
        if (null != objKeys)
            nm = objKeys.getString(stdName);
        return Strings.sBlank(nm, stdName);
    }

    public List<NutMap> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<NutMap> indexes) {
        this.indexes = indexes;
    }

    public String toString() {
        return String.format("%s:%s:%s", dao, tableName);
    }

}
