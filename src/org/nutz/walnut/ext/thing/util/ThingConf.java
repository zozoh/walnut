package org.nutz.walnut.ext.thing.util;

/**
 * 服务器端对 thing.js 的解析结果
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ThingConf {

    private ThingUniqueKey[] uniqueKeys;

    private ThingField[] fields;

    public boolean hasUniqueKeys() {
        return null != uniqueKeys && uniqueKeys.length > 0;
    }

    public ThingUniqueKey[] getUniqueKeys() {
        return uniqueKeys;
    }

    public void setUniqueKeys(ThingUniqueKey[] uniqueKeys) {
        this.uniqueKeys = uniqueKeys;
    }

    public ThingField[] getFields() {
        return fields;
    }

    public void setFields(ThingField[] fields) {
        this.fields = fields;
    }

}
