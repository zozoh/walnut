package com.site0.walnut.util;

import com.site0.walnut.api.io.WnObj;

public class WnObjCacheItem {

    private long expireAt;

    private WnObj obj;

    public WnObjCacheItem(WnObj obj, long expireAt) {
        this.expireAt = expireAt;
        this.obj = obj;
    }

    public boolean isExpired() {
        return expireAt > 0 && Wn.now() > expireAt;
    }

    public WnObj get() {
        return obj.clone();
    }

    public String id() {
        return obj.id();
    }

    public String path() {
        return obj.path();
    }
}
