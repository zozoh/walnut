package org.nutz.walnut.ext.lbs.bean;

import org.nutz.lang.util.NutMap;

public class LbsCountry {

    private String key;

    private NutMap name;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public NutMap getName() {
        return name;
    }

    public String getName(String lang, boolean dftFirst) {
        String name = this.name.getString(lang);
        if (dftFirst && null == name && this.name.size() > 0) {
            return this.name.entrySet().iterator().next().getValue().toString();
        }
        return name;
    }

    public void setName(NutMap name) {
        this.name = name;
    }

    public NutMap toMap(String lang) {
        NutMap map = new NutMap();
        String name = this.getName(lang, true);
        map.put("key", this.key);
        map.put("name", name);
        return map;
    }

}
