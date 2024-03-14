package com.site0.walnut.util;

import java.util.HashMap;
import java.util.Map;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;

public class WnObjDataCachedFactory<T> {

    private WnIo io;

    private Map<String, WnObjDataCachedItem<T>> mapping;

    public WnObjDataCachedFactory(WnIo io) {
        this.io = io;
        this.mapping = new HashMap<>();
    }

    public WnIo getIo() {
        return this.io;
    }

    public synchronized T get(WnObj o) {
        return this.get(o, null);
    }

    public synchronized T get(WnObj o, WnObjDataLoading<T> loading) {
        String oid = o.id();
        WnObjDataCachedItem<T> ci = mapping.get(oid);
        if (null == ci || !ci.isMatchFinger(io, o)) {
            // 看来要重现加载一遍
            if (null != loading) {
                T data = loading.load(o);
                this.set(o, data);
                return data;
            }
            // 嗯，那就智能清除映射了
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
            WnObjDataCachedItem<T> ci = new WnObjDataCachedItem<>();
            ci.setObj(io, o);
            ci.setData(data);
            mapping.put(oid, ci);
        }
    }

}
