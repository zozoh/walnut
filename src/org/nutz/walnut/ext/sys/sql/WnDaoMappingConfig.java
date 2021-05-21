package org.nutz.walnut.ext.sys.sql;

import java.util.List;

import org.nutz.lang.util.NutMap;

public class WnDaoMappingConfig extends WnDaoConfig {

    private boolean autoCreate;

    private List<NutMap> fields;

    private String[] pks;

    private String[] objKeys;

    private List<NutMap> indexes;

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

    public boolean hasObjKeys() {
        return null != objKeys;
    }

    public String[] getObjKeys() {
        return objKeys;
    }

    public void setObjKeys(String[] objKeys) {
        this.objKeys = objKeys;
    }

    public boolean hasIndexes() {
        return null != indexes && indexes.size() > 0;
    }

    public List<NutMap> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<NutMap> indexes) {
        this.indexes = indexes;
    }

}
