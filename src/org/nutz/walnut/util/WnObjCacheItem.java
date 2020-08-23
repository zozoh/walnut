package org.nutz.walnut.util;

import org.nutz.walnut.api.io.WnObj;

public class WnObjCacheItem {

    private long expireAt;

    private WnObj obj;

    public WnObjCacheItem(WnObj obj, long expireAt) {
        this.expireAt = expireAt;
        this.obj = obj;
    }

    public boolean isExpired() {
        return Wn.now() > expireAt;
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
