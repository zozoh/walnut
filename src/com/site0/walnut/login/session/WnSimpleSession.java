package com.site0.walnut.login.session;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.login.WnSession;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.val.id.WnSnowQMaker;

import java.util.Date;

public class WnSimpleSession implements WnSession {

    private final static WnSnowQMaker TicketMaker = new WnSnowQMaker(null, 10);

    private WnUser user;

    private String ticket;

    private long expiAt;

    private NutBean env;

    public WnSimpleSession() {
        this.env = new NutMap();
    }

    public WnSimpleSession(WnUser u, long duInMs) {
        this();
        this.ticket = TicketMaker.make(new Date(), null);
        this.user = u;
        this.expiAt = System.currentTimeMillis() + duInMs;
        this.env.putAll(u.getMeta());
    }

    public NutMap toBean() {
        NutMap re = new NutMap();
        this.mergeToBean(re);
        return re;
    }

    public void mergeToBean(NutBean bean) {
        bean.put("ticket", this.ticket);
        bean.put("expiAt", this.getExpiAtInUTC());
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
        Date d = new Date(expiAt);
        return Wtime.formatUTC(d, "yyyy-MM-dd HH:mm:ss");
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
