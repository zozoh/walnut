package org.nutz.walnut.ext.titanium;

import java.util.HashMap;
import java.util.Map;

import org.nutz.walnut.api.io.WnObj;

public class WnObjCachedFactory<T> {

    private Map<String, WnObjCachedItem<T>> mapping;

    public WnObjCachedFactory() {
        this.mapping = new HashMap<>();
    }

    public synchronized T get(WnObj o) {
        String oid = o.id();
        WnObjCachedItem<T> ci = mapping.get(oid);
        if (null == ci)
            return null;
        if (!ci.isMatchFinger(o)) {
            mapping.remove(oid);
            return null;
        }
        return ci.getData();
    }

    public synchronized void set(WnObj o, T data) {
        String oid = o.id();
        WnObjCachedItem<T> ci = new WnObjCachedItem<>();
        ci.setObj(o);
        ci.setData(data);
        mapping.put(oid, ci);
    }

}
