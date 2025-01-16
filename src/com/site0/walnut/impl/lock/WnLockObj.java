package com.site0.walnut.impl.lock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;

import org.nutz.lang.random.R;
import com.site0.walnut.api.lock.WnLock;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;

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

    private String name;

    private String hold;

    private String expi;

    private String privateKey;

    private String owner;

    private String hint;

    private static final String REG = "^(([^;]+))"
                                      + ";(name=([^,]*))"
                                      + ",(owner=([^,]*))"
                                      + ",(hold=([^,]*))"
                                      + ",(expi=([^,]*))"
                                      + ",(hint=(.*))"
                                      + "$";
    private static final Pattern _P = Pattern.compile(REG);

    // %s;name=%s,owner=%s,hold=%s,expi=%s,hint=%s
    // 78rr21;name=%s,owner=%s,hint=%s,hold=%s,expi=%s

    public void fromString(String str) {
        Matcher m = _P.matcher(str);
        if (m.find()) {
            this.privateKey = m.group(2);
            this.name = m.group(4);
            this.owner = m.group(6);
            this.hold = m.group(8);
            this.expi = m.group(10);
            this.hint = m.group(12);
        }
    }

    public String toString() {
        return String.format("%s;name=%s,owner=%s,hold=%s,expi=%s,hint=%s",
                             privateKey,
                             name,
                             owner,
                             this.hold,
                             this.expi,
                             hint);
    }

    @Override
    public WnLockObj clone() {
        WnLockObj lo = new WnLockObj();
        lo.name = name;
        lo.hold = hold;
        lo.expi = expi;
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
        if (Ws.isBlank(hold)) {
            return 0;
        }
        return Wtime.parseAMS(this.hold);
    }

    public void setHoldTime(long holdTime) {
        Date d = new Date(holdTime);
        this.hold = Wtime.formatUTC(d, "yyyy-MM-dd HH:mm:ss.SSS");
    }

    public long getExpiTime() {
        if (Ws.isBlank(expi)) {
            return 0;
        }
        return Wtime.parseAMS(this.expi);
    }

    public void setExpiTime(long expiTime) {
        Date d = new Date(expiTime);
        this.expi = Wtime.formatUTC(d, "yyyy-MM-dd HH:mm:ss.SSS");
    }

    public String getHold() {
        return hold;
    }

    public void setHold(String hold) {
        this.hold = hold;
    }

    public String getExpi() {
        return expi;
    }

    public void setExpi(String expi) {
        this.expi = expi;
    }

    @Override
    public boolean isExpired() {
        long now = Wn.now();
        return (now - this.getExpiTime()) > 0;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void genPrivateKey() {
        this.privateKey = R.captchaChar(10);
    }

    public boolean matchPrivateKey(String privateKey) {
        if (null == this.privateKey || null == privateKey) {
            return false;
        }
        return this.privateKey.equals(privateKey);
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
