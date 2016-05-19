package org.nutz.walnut.ext.qiniu;

import org.nutz.walnut.api.io.WnObj;

import com.qiniu.storage.BucketManager;
import com.qiniu.util.Auth;

public class QiniuWnObj {

    protected String ak;
    protected String sk;
    protected String bucket;
    protected String path;
    protected String domain;
    protected WnObj obj;

    public QiniuWnObj(WnObj obj) {
        this.ak = obj.getString("qiniu_ak");
        this.sk = obj.getString("qiniu_sk");
        this.bucket = obj.getString("qiniu_bucket");
        this.path = obj.getString("qiniu_path");
        this.domain = obj.getString("qiniu_domain");
        this.obj = obj;
    }

    public void copy2(WnObj obj) {
        for (String key : this.obj.keySet()) {
            if (!key.startsWith("qiniu_"))
                continue;
            obj.put(key, this.obj.get(key));
        }
    }

    public String getAk() {
        return ak;
    }

    public void setAk(String ak) {
        this.ak = ak;
    }

    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public WnObj getObj() {
        return obj;
    }

    public BucketManager bm() {
        return new BucketManager(Auth.create(ak, sk));
    }
}
