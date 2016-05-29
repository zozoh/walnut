package org.nutz.walnut.util;

import java.util.HashMap;
import java.util.Map;

import org.nutz.walnut.api.io.WnObj;

public class WnObjCache {

    private Map<String, WnObjCacheItem> byId;

    private Map<String, WnObjCacheItem> byPath;

    public WnObjCache() {
        byId = new HashMap<String, WnObjCacheItem>();
        byPath = new HashMap<String, WnObjCacheItem>();
    }

    public WnObj get(String id) {
        WnObjCacheItem oci = byId.get(id);
        return __check(oci);
    }

    private WnObj __check(WnObjCacheItem oci) {

        // 木有命中
        if (null == oci)
            return null;

        // 过期
        if (oci.isExpired()) {
            byId.remove(oci.id());
            byPath.remove(oci.path());
            return null;
        }

        // 返回一个 copy
        return oci.get();

    }

    public WnObj fetch(String ph) {
        WnObjCacheItem oci = byPath.get(ph);
        return __check(oci);
    }

    public void save(WnObj o, long duInMs) {
        long expi = System.currentTimeMillis() + duInMs;
        WnObjCacheItem item = new WnObjCacheItem(o, expi);
        byId.put(o.id(), item);
        byPath.put(o.path(), item);
    }

    public void clear() {
        byId.clear();
        byPath.clear();
    }

    public void remove(WnObj o) {
        byId.remove(o.id());
        byPath.remove(o.path());
    }

}
