package com.site0.walnut.cache.temp;

import com.site0.walnut.util.Wtime;
import java.util.Date;

public class WnInterimCacheItem<T> {

    private String key;
    private T data;
    private long expireAt;
    private long duInMs;

    public WnInterimCacheItem(String key, T data, long duInMs) {
        this.key = key;
        this.data = data;
        this.duInMs = duInMs;
        this.expireAt = System.currentTimeMillis() + duInMs;
    }

    public String toString() {
        String ds = Wtime.format(new Date(expireAt), "yy-MM-dd HH:mm:ss.SSS");
        String str = null == data ? "null" : data.toString();
        if (str.length() > 15) {
            str = str.substring(0, 15) + "...";
        }
        return String.format("[%s](%s):%s", key, ds, str);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 检查缓存项是否已过期
     * 
     * @return true 如果当前时间超过过期时间，否则 false
     */
    public boolean isExpired() {
        long now = System.currentTimeMillis();
        return isExpired(now);
    }

    public boolean isExpired(long nowInMs) {
        return nowInMs > this.expireAt;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(long expireAt) {
        this.expireAt = expireAt;
    }

    /**
     * 更新缓存项的过期时间（通常发生在访问时）
     * 
     * @return 更新后的过期时间戳（毫秒）
     */
    public long touch() {
        long now = System.currentTimeMillis();
        return touch(now);
    }

    public long touch(long now) {
        this.expireAt = now + duInMs;
        return this.expireAt;
    }

    public long getDuInMs() {
        return duInMs;
    }

    public void setDuInMs(long duInMs) {
        this.duInMs = duInMs;
    }

}
