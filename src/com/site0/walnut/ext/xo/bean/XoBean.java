package com.site0.walnut.ext.xo.bean;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnRace;

public class XoBean {

    /**
     * 通过对象的 Common Prefix 得来的，通常为路径 但是它并不具备真正的记录
     */
    private boolean virtual;

    private String key;

    private String etag;

    private long size;

    private String storageClass;

    private Date lastModified;

    private String title;

    private String mime;

    private Instant expires;

    private NutMap rawMeta;

    private NutMap userMeta;

    public XoBean() {
        this.rawMeta = new NutMap();
        this.userMeta = new NutMap();
    }

    public String toString() {
        return String.format("%s[eTag=%s]size=%s(%s)\nrawMeta= %s\nusrMeta= %s",
                             key,
                             etag,
                             size,
                             storageClass,
                             Json.toJson(rawMeta),
                             Json.toJson(userMeta));
    }

    public WnRace getRace() {
        if (null == key) {
            return null;
        }
        if (null != key && key.endsWith("/")) {
            return WnRace.DIR;
        }
        return WnRace.FILE;
    }

    public boolean isDIR() {
        return WnRace.DIR == getRace();
    }

    public boolean isFILE() {
        return WnRace.FILE == getRace();
    }

    public String getName() {
        if (null == key) {
            return null;
        }
        if (key.endsWith("/")) {
            int len = key.length();
            String path = key.substring(0, len - 1);
            return Files.getName(path);
        }
        return Files.getName(key);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public void setVirtual(boolean virtual) {
        this.virtual = virtual;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public boolean hasRawMeta() {
        return null != this.rawMeta && !rawMeta.isEmpty();
    }

    public NutMap getRawMeta() {
        return rawMeta;
    }

    public NutMap rawMeta() {
        if (null == this.rawMeta) {
            this.rawMeta = new NutMap();
        }
        return rawMeta;
    }

    public void setRawMeta(NutMap rawMeta) {
        this.rawMeta = rawMeta;
    }

    public void putAllRawMeta(Map<String, ? extends Object> meta) {
        this.rawMeta.putAll(meta);
    }

    public boolean hasUserMeta() {
        return null != this.userMeta && !userMeta.isEmpty();
    }

    public NutMap getUserMeta() {
        return userMeta;
    }

    public NutMap userMeta() {
        if (null == this.userMeta) {
            this.userMeta = new NutMap();
        }
        return userMeta;
    }

    public void setUserMeta(NutMap userMeta) {
        this.userMeta = userMeta;
    }

    public void putAllUserMeta(Map<String, ? extends Object> meta) {
        this.userMeta.putAll(meta);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public Instant getExpires() {
        return expires;
    }

    public void setExpires(Instant expires) {
        this.expires = expires;
    }
    
    

}
