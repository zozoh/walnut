package org.nutz.walnut.ext.thing.util;

import java.util.Map;

import org.nutz.json.JsonField;
import org.nutz.lang.util.NutBean;

/**
 * 服务器端对 thing.js 的解析结果
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ThingConf {

    private ThingUniqueKey[] uniqueKeys;

    private ThingField[] fields;

    @JsonField("lnKeys")
    private Map<String, ThingLinkKey> linkKeys;

    @JsonField("on_create")
    private String onCreate;

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

    public String getOnCreate() {
        return onCreate;
    }

    public void setOnCreate(String onCreate) {
        this.onCreate = onCreate;
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
