package com.site0.walnut.cache.temp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.site0.walnut.cache.WnCache;

public class WnInterimCache<T> implements WnCache<T> {

    private int cleanThreshold;

    private long duInMs;

    private long last_put_in_ms;

    private Map<String, WnInterimCacheItem<T>> map;

    private boolean touchWhenGet;

    public WnInterimCache(int duInSec) {
        this(duInSec, 100);
    }

    public WnInterimCache(int duInSec, int cleanThreshold) {
        this.duInMs = duInSec * 1000L;
        this.cleanThreshold = cleanThreshold;
        this.map = new WeakHashMap<>();
    }

    @Override
    public String toString() {
        String str = String.format("InterimCache size=%d (%d) itemDu=%ds\n",
                                   map.size(),
                                   cleanThreshold,
                                   duInMs);
        StringBuilder sb = new StringBuilder(str);

        Set<Map.Entry<String, WnInterimCacheItem<T>>> ens = map.entrySet();
        int n = Math.min(10, ens.size());
        int i = 0;
        for (Map.Entry<String, WnInterimCacheItem<T>> en : ens) {
            i++;
            String s = String.format("  - [%d]: %s\n", i, en.getValue());
            sb.append(s);
            if (i >= n) {
                break;
            }
        }
        if (i < ens.size()) {
            sb.append("...\n");
        }
        return sb.toString();
    }

    @Override
    synchronized public T get(String key) {
        if (null == key) {
            return null;
        }
        WnInterimCacheItem<T> it = map.get(key);
        if (null == it) {
            return null;
        }
        long now = System.currentTimeMillis();
        if (it.isExpired(now)) {
            map.remove(it.getKey());
            return null;
        }
        if (this.touchWhenGet) {
            it.touch(now);
        }
        return it.getData();
    }

    @Override
    synchronized public void put(String key, T data) {
        if (null == key || null == data) {
            return;
        }
        long now = System.currentTimeMillis();
        WnInterimCacheItem<T> it = map.get(key);
        if (null == it) {
            it = new WnInterimCacheItem<>(key, data, duInMs);
            map.put(key, it);
        } else {
            it.setDuInMs(duInMs);
            it.setExpireAt(now + duInMs);
            it.setData(data);
        }

        // 清理过期项目
        if (last_put_in_ms > 0 && map.size() > this.cleanThreshold) {
            long cooling = now - last_put_in_ms;
            if (cooling > duInMs) {
                __clean_up(now);
            }
        }
        last_put_in_ms = now;
    }

    @Override
    synchronized public T remove(String key) {
        WnInterimCacheItem<T> it = map.remove(key);
        return null == it ? null : it.getData();
    }

    @Override
    synchronized public void cleanUp() {
        long now = System.currentTimeMillis();
        __clean_up(now);
    }

    private void __clean_up(long nowInMs) {
        List<String> keys = new ArrayList<>(map.size());
        for (WnInterimCacheItem<T> it : map.values()) {
            if (it.isExpired(nowInMs)) {
                keys.add(it.getKey());
            }
        }
        for (String k : keys) {
            map.remove(k);
        }
    }

    @Override
    synchronized public void clearAll() {
        map.clear();

    }

    @Override
    synchronized public int size() {
        return map.size();
    }

    public int getCleanThreshold() {
        return cleanThreshold;
    }

    public void setCleanThreshold(int cleanThreshold) {
        this.cleanThreshold = cleanThreshold;
    }

    public long getDuInMs() {
        return duInMs;
    }

    public void setDuInMs(long duInMs) {
        this.duInMs = duInMs;
    }

    public long getLast_put_in_ms() {
        return last_put_in_ms;
    }

    public void setLast_put_in_ms(long last_put_in_ms) {
        this.last_put_in_ms = last_put_in_ms;
    }

    public Map<String, WnInterimCacheItem<T>> getMap() {
        return map;
    }

    public void setMap(Map<String, WnInterimCacheItem<T>> map) {
        this.map = map;
    }

    public boolean isTouchWhenGet() {
        return touchWhenGet;
    }

    public void setTouchWhenGet(boolean touchWhenGet) {
        this.touchWhenGet = touchWhenGet;
    }

}
