package org.nutz.walnut.api.io;

import java.util.Date;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Wn;

/**
 * 记录一个对象内容修改的一条历史记录
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnHistory extends NutMap {

    public WnHistory() {}

    public WnHistory(WnObj o) {
        owner(Wn.WC().checkMe());
        oid(o.id());
        createTime(o.lastModified());
        sha1(o.sha1());
        len(o.len());
    }

    public String oid() {
        return this.getString("oid");
    }

    public WnHistory oid(String oid) {
        this.setOrRemove("oid", oid);
        return this;
    }

    public boolean isLastHistory(WnObj o) {
        if (null == o)
            return false;
        return sha1().equals(o.sha1())
               && this.createTime().equals(o.lastModified());
    }

    public String sha1() {
        return this.getString("sha1");
    }

    public WnHistory sha1(String sha1) {
        this.setOrRemove("sha1", sha1);
        return this;
    }

    public boolean isSameSha1(String sha1) {
        if (null == sha1)
            return false;
        String mySha1 = sha1();
        if (null == mySha1)
            return false;
        return mySha1.equals(sha1);
    }

    public String owner() {
        return this.getString("ow");
    }

    public WnHistory owner(String ow) {
        this.setOrRemove("ow", ow);
        return this;
    }

    public long len() {
        return this.getLong("len");
    }

    public WnHistory len(long len) {
        this.setOrRemove("len", len);
        return this;
    }

    public Date createTime() {
        return this.getAs("ct", Date.class);
    }

    public WnHistory createTime(Date ct) {
        this.setOrRemove("ct", ct);
        return this;
    }

}
