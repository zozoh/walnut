package org.nutz.walnut.ext.data.titanium.util;

import java.util.HashMap;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;

public class TiDictFactory {

    private Map<String, TiDict> dicts;

    public TiDictFactory() {
        dicts = new HashMap<>();
    }

    public TiDictFactory(Map<String, Object> map) {
        this();
        this.load(map);
    }

    public TiDictFactory(String json) {
        this();
        NutMap map = Json.fromJson(NutMap.class, json);
        this.load(map);
    }

    public TiDictFactory load(Map<String, Object> map) {
        NutMap m2 = NutMap.WRAP(map);
        for (String name : m2.keySet()) {
            TiDict dict = m2.getAs(name, TiDict.class);
            if (null != dict) {
                dicts.put(name, dict);
            }
        }
        return this;
    }

    public void clear() {
        dicts.clear();
    }

    public boolean hasDict(String name) {
        return dicts.get(name) != null;
    }

    public TiDict getDict(String name) {
        if (null == name) {
            return null;
        }
        if (name.startsWith("#")) {
            name = name.substring(1).trim();
        }
        return dicts.get(name);
    }

}
