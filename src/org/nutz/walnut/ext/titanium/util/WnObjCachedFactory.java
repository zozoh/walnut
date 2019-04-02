package org.nutz.walnut.ext.titanium.util;

import java.util.HashMap;
import java.util.Map;

import org.nutz.walnut.api.io.WnObj;

public class WnObjCachedFactory<T> {

    private Map<String, WnObjCachedItem<T>> mapping;

    public WnObjCachedFactory() {
        this.mapping = new HashMap<>();
    }

    public synchronized T get(WnObj o) {
        return this.get(o, null);
    }

    public synchronized T get(WnObj o, WnObjDataLoading<T> loading) {
        String oid = o.id();
        WnObjCachedItem<T> ci = mapping.get(oid);
        if (null == ci || !ci.isMatchFinger(o)) {
            if (null != loading) {
                T data = loading.load(o);
                this.set(o, data);
                return data;
            }
            // 清除映射
            if (null != ci)
                mapping.remove(oid);
            return null;
        }
        return ci.getData();
    }

    public synchronized void set(WnObj o, T data) {
        String oid = o.id();
        // 移除
        if (null == data) {
            mapping.remove(oid);
        }
        // 设置
        else {
            WnObjCachedItem<T> ci = new WnObjCachedItem<>();
            ci.setObj(o);
            ci.setData(data);
            mapping.put(oid, ci);
        }
    }

}
