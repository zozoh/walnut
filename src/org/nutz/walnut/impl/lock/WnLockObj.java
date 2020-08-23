package org.nutz.walnut.impl.lock;

import org.nutz.json.JsonField;
import org.nutz.lang.random.R;
import org.nutz.walnut.api.lock.WnLock;
import org.nutz.walnut.util.Wn;

/**
 * 普通锁对象（仅是一个 POJO）
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnLockObj implements WnLock {

    @JsonField("nm")
    private String name;

    @JsonField("hold")
    private long holdTime;

    @JsonField("expi")
    private long expiTime;

    @JsonField("pkey")
    private String privateKey;

    @JsonField("ow")
    private String owner;

    private String hint;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getHoldTime() {
        return holdTime;
    }

    public void setHoldTime(long holdTime) {
        this.holdTime = holdTime;
    }

    public long getExpiTime() {
        return expiTime;
    }

    public void setExpiTime(long expiTime) {
        this.expiTime = expiTime;
    }

    @Override
    public boolean isExpired() {
        long now = Wn.now();
        return (now - this.expiTime) > 0;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void genPrivateKey() {
        this.privateKey = R.captchaChar(10);
    }

    public void genPrivateKey(int len) {
        this.privateKey = R.captchaChar(len);
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public boolean isSame(WnLock lock) {
        if (null == lock)
            return false;

        if (!name.equals(lock.getName()))
            return false;

        if (!owner.equals(lock.getOwner()))
            return false;

        if (!privateKey.equals(lock.getPrivateKey()))
            return false;

        return true;
    }

}
