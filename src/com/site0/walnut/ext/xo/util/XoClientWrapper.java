package com.site0.walnut.ext.xo.util;

import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

public abstract class XoClientWrapper<T> {

    private String key;

    private T client;

    private long expiredAt;

    private String region;

    private String bucket;

    private String prefix;

    public XoClientWrapper(String key) {
        this.key = key;
    }

    protected abstract void _close_client(T client);

    public void close() {
        if (null != client) {
            _close_client(client);
        }
    }

    public String getDirPath(String dirKey) {
        String folder = dirKey.endsWith("/") ? dirKey : dirKey + "/";
        return getObjPath(folder);
    }

    public String getObjPath(String path) {
        if ("*".equals(path)) {
            path = null;
        }
        String obj_path = path;
        if (!Ws.isBlank(prefix)) {
            if (Ws.isBlank(path)) {
                obj_path = this.prefix;
            } else {
                obj_path = Wn.appendPath(this.prefix, path);
                if (path.endsWith("/") && !obj_path.endsWith("/")) {
                    obj_path += "/";
                }
            }
        }
        return obj_path;
    }

    public String getQueryPrefix(String objKey, String delimiter) {
        String queryPath = this.getObjPath(objKey);
        // 删除结尾的 *， 用 'a/*' 来搜索，可能更加符合直觉
        if (null != queryPath && queryPath.endsWith("*")) {
            queryPath = queryPath.substring(0, queryPath.length() - 1).trim();
        }
        // 确保路径以/结尾（如果是目录）
        if (null != delimiter
            && delimiter.length() > 0
            && objKey != null
            && objKey.endsWith(delimiter)
            && !queryPath.endsWith(delimiter)) {
            queryPath += delimiter;
        }
        return queryPath;
    }

    public String toMyKey(String key) {
        if (!Ws.isBlank(prefix) && key.startsWith(this.prefix)) {
            return key.substring(this.prefix.length());
        }
        return key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getClient() {
        return client;
    }

    public void setClient(T client) {
        this.client = client;
    }

    public boolean isExpired() {
        return isExpired(System.currentTimeMillis());
    }

    public boolean isExpired(long now) {
        return now > this.expiredAt;
    }

    public long getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(long expiredAt) {
        this.expiredAt = expiredAt;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public boolean hasPrefix() {
        return !Ws.isBlank(prefix);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        if (Ws.isBlank(prefix)) {
            this.prefix = null;
        } else if (prefix.endsWith("*")) {
            this.prefix = prefix.substring(0, prefix.length() - 1).trim();
        } else {
            this.prefix = prefix;
        }
    }

    public String[] getAllowPrefixes() {
        if (null == prefix) {
            return Wlang.array("*");
        }
        return Wlang.array(Wn.appendPath(prefix, "*"));

    }

}
