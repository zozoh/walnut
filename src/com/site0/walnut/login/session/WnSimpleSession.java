package com.site0.walnut.login.session;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.login.WnSession;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.val.id.WnSnowQMaker;

import java.util.Date;
import java.util.Map;

public class WnSimpleSession implements WnSession {

    private final static WnSnowQMaker TicketMaker = new WnSnowQMaker(null, 10);

    private WnUser user;

    /**
     * 会话票据
     */
    private String ticket;

    /**
     * 父会话票据
     */
    private String parentTicket;

    private long expiAt;

    private long createTime;

    private long lastModified;

    private NutBean env;

    public WnSimpleSession() {
        this.env = new NutMap();
    }

    public WnSimpleSession(WnUser u, long duInMs) {
        this();
        long now = System.currentTimeMillis();
        this.ticket = TicketMaker.make(new Date(), null);
        this.user = u;
        this.expiAt = now + duInMs;
        this.createTime = now;
        this.lastModified = now;
        this.env.putAll(u.getMeta());
    }

    @Override
    public WnSession clone() {
        WnSimpleSession re = new WnSimpleSession();
        re.ticket = this.ticket;
        re.user = this.user.clone();
        re.expiAt = this.expiAt;
        re.createTime = this.createTime;
        re.lastModified = this.lastModified;
        re.env = new NutMap();
        re.env.putAll(this.env);
        return re;
    }

    @Override
    public String getMyName() {
        if (null == this.user) {
            return null;
        }
        return this.user.getName();
    }

    @Override
    public String getMyGroup() {
        if (null == this.user) {
            return null;
        }
        return this.user.getMainGroup();
    }

    @Override
    public boolean isSameTicket(String ticket) {
        if (null == ticket) {
            return false;
        }
        return ticket.equals(this.ticket);
    }

    public String toJson(JsonFormat fmt) {
        NutMap map = this.toBean();
        return Json.toJson(map, fmt);
    }

    @Override
    public NutMap toBean() {
        NutMap re = new NutMap();
        this.mergeToBean(re);
        return re;
    }

    public void mergeToBean(NutBean bean) {
        bean.put("ticket", this.ticket);
        bean.put("expiAt", this.getExpiAtInUTC());
        bean.put("createTime", this.getCreateTimeInUTC());
        bean.put("lastModified", this.getLastModifiedInUTC());
        bean.put("user", user.toBean());
        bean.put("env", env);
    }

    public String toString() {
        String expiAtInStr = Wtime.format(new Date(this.expiAt), "yyyy-MM-dd HH:mm:ss z");
        return String.format("session<%s> user=%s, expiAt=",
                             ticket,
                             null == user ? "null" : user.toString(),
                             expiAtInStr);
    }

    public WnUser getUser() {
        return user;
    }

    public void setUser(WnUser user) {
        this.user = user;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    @Override
    public boolean hasParentTicket() {
        return !Ws.isBlank(parentTicket);
    }

    @Override
    public String getParentTicket() {
        return parentTicket;
    }

    @Override
    public void setParentTicket(String parentTicket) {
        this.parentTicket = parentTicket;
    }

    public boolean isExpired() {
        long now = System.currentTimeMillis();
        return now > this.expiAt;
    }

    public long getExpiAt() {
        return expiAt;
    }

    public void setExpiAt(long expiAt) {
        this.expiAt = expiAt;
    }

    public String getExpiAtInUTC() {
        if (this.expiAt <= 0) {
            return null;
        }
        Date d = new Date(expiAt);
        return Wtime.formatUTC(d, "yyyy-MM-dd HH:mm:ss");
    }

    public void setExpiAtInUTC(Object utcTime) {
        this.expiAt = Wtime.parseAnyAMSUTC(utcTime);
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getCreateTimeInUTC() {
        if (this.createTime <= 0) {
            return null;
        }
        Date d = new Date(this.createTime);
        return Wtime.formatUTC(d, "yyyy-MM-dd HH:mm:ss");
    }

    public void setCreateTimeInUTC(Object utcTime) {
        this.createTime = Wtime.parseAnyAMSUTC(utcTime);
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getLastModifiedInUTC() {
        if (this.lastModified <= 0) {
            return null;
        }
        Date d = new Date(this.lastModified);
        return Wtime.formatUTC(d, "yyyy-MM-dd HH:mm:ss");
    }

    public void setLastModifiedInUTC(Object utcTime) {
        this.lastModified = Wtime.parseAnyAMSUTC(utcTime);
    }

    @Override
    public void loadEnvFromUser(WnUser u) {
        if (u.hasMeta()) {
            for (Map.Entry<String, Object> en : u.getMeta().entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                this.env.put(key, val);
            }
        }
    }

    public NutBean getEnv() {
        return env;
    }

    @Override
    public String getEnvAsStr() {
        JsonFormat jfmt = JsonFormat.compact().setQuoteName(true);
        return Json.toJson(env, jfmt);
    }

    public void setEnv(NutBean env) {
        this.env = env;
    }

    public void updateEnv(String key, Object val) {
        this.env.put(key, val);
    }

    public void updateEnv(NutBean delta) {
        this.env.putAll(delta);
    }

}
