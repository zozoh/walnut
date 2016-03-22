package org.nutz.walnut.ext.qiniu;

import java.io.IOException;
import java.io.InputStream;

import org.nutz.http.Request;
import org.nutz.http.Request.METHOD;
import org.nutz.http.Response;
import org.nutz.http.Sender;
import org.nutz.lang.Lang;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnBucketBlockInfo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.io.AbstractBucket;

import com.qiniu.storage.BucketManager;
import com.qiniu.util.Auth;

public class QininBucket extends AbstractBucket {

    private static final Log log = Logs.get();

    protected WnObj obj;
    protected BucketManager bm;
    protected Auth auth;
    protected String bucket;
    protected String domain;

    public QininBucket(QiniuWnObj obj) {
        String ak = obj.getAk();
        String sk = obj.getSk();
        auth = Auth.create(ak, sk);
        bm = new BucketManager(auth);
        domain = obj.getDomain();
        this.obj = obj.getObj();
    }

    protected String path() {
        return obj.getString("qiniu_path");
    }

    protected String bucket() {
        return obj.getString("qiniu_bucket");
    }

    public String getId() {
        return "qiniu://" + bucket() + "/" + path();
    }

    public boolean isSealed() {
        return false;
    }

    public long getCreateTime() {
        return obj.createTime();
    }

    public long getLastModified() {
        return obj.lastModified();
    }

    public long getLastReaded() {
        return obj.lastModified();
    }

    public long getLastWrited() {
        return obj.lastModified();
    }

    public long getLastSealed() {
        return obj.lastModified();
    }

    public long getLastOpened() {
        return obj.lastModified();
    }

    public long getCountRefer() {
        return 1;
    }

    public long getCountRead() {
        return 1;
    }

    public int getBlockSize() {
        return (int) obj.len();
    }

    public int getBlockNumber() {
        return 0;
    }

    public String getParentBucketId() {
        return null;
    }

    public void setParentBucketId(String pbid) {}

    public boolean isDuplicated() {
        return false;
    }

    public long getSize() {
        return obj.len();
    }

    public String getSha1() {
        return obj.sha1();
    }

    public int read(int index, byte[] bs, WnBucketBlockInfo bi) {
        long pos = index * getBlockSize();
        int pl = 0;
        int sz = read(pos, bs, 0, bs.length);
        int pr = bs.length - sz;

        if (null != bi)
            bi.set(pl, sz, pr);

        return sz;
    }

    public int read(long pos, byte[] bs, int off, int len) {
        String url = auth.privateDownloadUrl(domain + "/" + path(), 300);
        Request req = Request.create(url, METHOD.GET);
        req.getHeader().set("Range", "bytes=" + pos + "-").remove("Accept-Encoding");
        log.debugf("qiniu read %s pos=%s off=%d len=%d", path(), pos, off, len);
        Response resp = Sender.create(req).setTimeout(30 * 1000).send();
        if (resp.getStatus() == 200 || resp.getStatus() == 206) {
            try (InputStream ins = resp.getStream();) {
                int count = 0;
                while (len > count) {
                    int _len = ins.read(bs, off + count, len - count);
                    if (_len == -1)
                        break;
                    count += _len;
                }
                log.debug("qiniu read Headers\n" + resp.getHeader());
                log.debug("qiniu resp code=" + resp.getStatus());
                log.debugf("qiniu read %s pos=%s off=%d len=%d count=%d",
                           path(),
                           pos,
                           off,
                           len,
                           count);
                return count;
            }
            catch (IOException e) {
                log.debug("qiniu io error", e);
                return -1;
            }
        }
        log.debug("qiniu resp=" + resp.getStatus());
        return -1;
    }

    public int write(int index, int padding, byte[] bs, int off, int len) {
        throw Lang.noImplement();
    }

    public void trancateBlock(int nb) {
        throw Lang.noImplement();
    }

    public void trancateSize(long size) {
        throw Lang.noImplement();
    }

    public String seal() {
        return getSha1();
    }

    public void unseal() {}

    public void update() {}

    public WnBucket duplicateVirtual() {
        return new QininBucket(new QiniuWnObj(obj));
    }

    public long refer() {
        return 1;
    }

    public long free() {
        return 1;
    }

}
