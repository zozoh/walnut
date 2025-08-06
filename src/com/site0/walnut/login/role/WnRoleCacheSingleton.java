package com.site0.walnut.login.role;

import java.util.HashMap;
import java.util.Map;

import com.site0.walnut.cache.WnCache;
import com.site0.walnut.cache.temp.WnInterimCache;
import com.site0.walnut.login.WnLoginCacheOptions;

public class WnRoleCacheSingleton {

    private static final WnRoleCacheSingleton _one = new WnRoleCacheSingleton();

    public static WnRoleCacheSingleton me() {
        return _one;
    }

    private Map<String, WnCache<WnRoleList>> map;

    public WnRoleCacheSingleton() {
        map = new HashMap<>();
    }

    public WnCache<WnRoleList> getCache(String key,
                                        int minKeep,
                                        int cleanThreshold,
                                        int duInSec) {
        WnCache<WnRoleList> cache = map.get(key);
        if (cleanThreshold <= 0) {
            cleanThreshold = 500;
        }
        if (minKeep <= 0) {
            minKeep = Math.min(100, cleanThreshold / 2);
        }
        if (duInSec <= 1) {
            duInSec = 3;
        }
        if (null == cache) {
            synchronized (this) {
                cache = map.get(key);
                if (null == cache) {
                    cache = new WnInterimCache<>(duInSec, cleanThreshold);
                    map.put(key, cache);
                }
            }
        }
        return cache;
    }

    public WnCache<WnRoleList> getCache(String key) {
        return getCache(key, 0, 0, 0);
    }

    public WnCache<WnRoleList> getCache(String key, WnLoginCacheOptions options) {
        if (null == options) {
            return getCache(key);
        }
        return getCache(key, options.minKeep, options.cleanThreshold, options.duInSec);
    }

}
