package org.nutz.walnut.ext.thing.util;

import java.util.Map;

import org.nutz.lang.util.NutBean;

/**
 * 服务器端对 thing.json 的解析结果
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ThingConf {

    private ThingUniqueKey[] uniqueKeys;

    private ThingField[] fields;

    // zozoh: 先去掉这两个奇怪的键名，用原生的，看看会发生什么
    // @JsonField("lnKeys")
    private Map<String, ThingLinkKey> linkKeys;

    private String[] onCreated;

    private String[] onBeforeUpdate;

    private String[] onUpdated;

    private String[] onBeforeDelete;

    private String[] onDeleted;

    public boolean hasUniqueKeys() {
        return null != uniqueKeys && uniqueKeys.length > 0;
    }

    public ThingUniqueKey[] getUniqueKeys() {
        return uniqueKeys;
    }

    public void setUniqueKeys(ThingUniqueKey[] uniqueKeys) {
        this.uniqueKeys = uniqueKeys;
    }

    public boolean hasLinkKeys() {
        return null != linkKeys && linkKeys.size() > 0;
    }

    public Map<String, ThingLinkKey> getLinkKeys() {
        return linkKeys;
    }

    public void setLinkKeys(Map<String, ThingLinkKey> linkKeys) {
        this.linkKeys = linkKeys;
    }

    public String[] getOnCreated() {
        return onCreated;
    }

    public void setOnCreated(String[] onCreate) {
        this.onCreated = onCreate;
    }

    public String[] getOnBeforeUpdate() {
        return onBeforeUpdate;
    }

    public void setOnBeforeUpdate(String[] onBeforeUpdate) {
        this.onBeforeUpdate = onBeforeUpdate;
    }

    public String[] getOnUpdated() {
        return onUpdated;
    }

    public void setOnUpdated(String[] onUpdated) {
        this.onUpdated = onUpdated;
    }

    public String[] getOnBeforeDelete() {
        return onBeforeDelete;
    }

    public void setOnBeforeDelete(String[] onBeforeDelete) {
        this.onBeforeDelete = onBeforeDelete;
    }

    public String[] getOnDeleted() {
        return onDeleted;
    }

    public void setOnDeleted(String[] onDeleted) {
        this.onDeleted = onDeleted;
    }

    public ThingField[] getFields() {
        return fields;
    }

    public void setFields(ThingField[] fields) {
        this.fields = fields;
    }

    public void validate(NutBean meta, boolean ignoreNoExists) {
        if (null != fields) {
            for (ThingField fld : fields) {
                if (ignoreNoExists && !meta.containsKey(fld.getKey()))
                    continue;
                fld.validate(meta);
            }
        }
    }

}
