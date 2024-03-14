package com.site0.walnut.impl.lock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.JsonField;
import org.nutz.lang.random.R;
import com.site0.walnut.api.lock.WnLock;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

/**
 * 普通锁对象（仅是一个 POJO）
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnLockObj implements WnLock {
    
    public static WnLockObj create(String name, String str) {
        WnLockObj lo = new WnLockObj();
        lo.fromString(str);
        lo.setName(name);
        return lo;
    }

    public static WnLockObj create(String str) {
        WnLockObj lo = new WnLockObj();
        lo.fromString(str);
        return lo;
    }

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

    private static final String REG = "^(([^=]+)=)?([^:]+)(:([^#]+))?(#(.+))?$";
    private static final Pattern _P = Pattern.compile(REG);

    // ${name}=${owner}:${privateKey}#${hint}
    // LOCK_TASK_QUEUE=node73:78rr21#push_task

    public void fromString(String str) {
        Matcher m = _P.matcher(str);
        if (m.find()) {
            this.name = m.group(2);
            this.owner = m.group(3);
            this.privateKey = m.group(5);
            this.hint = m.group(7);
        }
    }

    public String toString() {
        return this.toValue(true, true);
    }

    public String toValue() {
        return this.toValue(false, false);
    }

    public String toValue(boolean withName, boolean withHint) {
        StringBuilder sb = new StringBuilder();
        if (withName) {
            sb.append(name).append(':');
        }
        sb.append(owner);
        if (!Ws.isBlank(privateKey)) {
            sb.append(':').append(privateKey);
        }
        if (withHint && !Ws.isBlank(hint)) {
            sb.append('#').append(hint);
        }
        return sb.toString();
    }

    public WnLockObj clone() {
        WnLockObj lo = new WnLockObj();
        lo.name = name;
        lo.holdTime = holdTime;
        lo.expiTime = expiTime;
        lo.privateKey = privateKey;
        lo.owner = owner;
        lo.hint = hint;
        return lo;
    }

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
