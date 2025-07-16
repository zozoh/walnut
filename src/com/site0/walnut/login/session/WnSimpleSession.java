package com.site0.walnut.login.session;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.login.usr.WnUser;
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

    /**
     * 当子会话退出登录它就会被立即删除。 这时，它会把自己的票据临时记录在这个字段里 这样，当 /u/ajax/chse 被调用时，才能有办法
     * 检查本次会话转换是否合理
     */
    private String childTicket;

    /**
     * 持续时间（秒）每次 touch 都会在当前时间加上一个持续时间
     */
    private int duration;

    private long expiAt;

    private long createTime;

    private long lastModified;

    private NutBean env;

    public WnSimpleSession() {
        this.env = new NutMap();
    }

    public WnSimpleSession(WnUser u, int duInSec) {
        this();
        long now = System.currentTimeMillis();
        this.ticket = TicketMaker.make(new Date(), null);
        this.user = u;
        this.expiAt = now + duInSec * 1000L;
        this.duration = duInSec;
        this.createTime = now;
        this.lastModified = now;
        this.loadEnvFromUser(u);
    }

    @Override
    public WnSession clone() {
        WnSimpleSession re = new WnSimpleSession();
        re.ticket = this.ticket;
        re.parentTicket = this.parentTicket;
        re.childTicket = this.childTicket;
        re.duration = this.duration;
        re.user = this.user.clone();
        re.expiAt = this.expiAt;
        re.createTime = this.createTime;
        re.lastModified = this.lastModified;
        re.env = new NutMap();
        re.env.putAll(this.env);
        return re;
    }

    @Override
    public String getMyId() {
        if (null == this.user) {
            return null;
        }
        return this.user.getId();
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
    public boolean isSame(WnSession se) {
        return isSameTicket(se.getTicket());
    }

    @Override
    public boolean isSameTicket(String ticket) {
        if (null == ticket || null == this.ticket) {
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
        bean.put("parentTicket", this.parentTicket);
        bean.put("childTicket", this.childTicket);
        bean.put("duration", this.duration);
        bean.put("expiAt", this.getExpiAtInUTC());
        bean.put("createTime", this.getCreateTimeInUTC());
        bean.put("lastModified", this.getLastModifiedInUTC());
        bean.put("me", user.toBean());
        if (null != user) {
            bean.put("unm", user.getName());
        }
        bean.put("envs", env);
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
        this.loadEnvFromUser(user);
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
    public boolean isParentOf(WnSession se) {
        if (!se.hasParentTicket()) {
            return false;
        }
        return se.getParentTicket().equals(this.ticket);
    }

    @Override
    public void setParentTicket(String parentTicket) {
        this.parentTicket = parentTicket;
    }

    @Override
    public boolean hasChildTicket() {
        return !Ws.isBlank(childTicket);
    }

    @Override
    public String getChildTicket() {
        return childTicket;
    }

    @Override
    public void setChildTicket(String childTicket) {
        this.childTicket = childTicket;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public long getDurationInMs() {
        return duration * 1000L;
    }

    @Override
    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public boolean isExpired() {
        long now = System.currentTimeMillis();
        return now > this.expiAt;
    }

    @Override
    public long getExpiAt() {
        return expiAt;
    }

    @Override
    public void setExpiAt(long expiAt) {
        this.expiAt = expiAt;
    }

    @Override
    public String getExpiAtInUTC() {
        if (this.expiAt <= 0) {
            return null;
        }
        Date d = new Date(expiAt);
        return Wtime.formatUTC(d, "yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public void setExpiAtInUTC(Object utcTime) {
        this.expiAt = Wtime.parseAnyAMSUTC(utcTime);
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public String getCreateTimeInUTC() {
        if (this.createTime <= 0) {
            return null;
        }
        Date d = new Date(this.createTime);
        return Wtime.formatUTC(d, "yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public void setCreateTimeInUTC(Object utcTime) {
        this.createTime = Wtime.parseAnyAMSUTC(utcTime);
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String getLastModifiedInUTC() {
        if (this.lastModified <= 0) {
            return null;
        }
        Date d = new Date(this.lastModified);
        return Wtime.formatUTC(d, "yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public void setLastModifiedInUTC(Object utcTime) {
        this.lastModified = Wtime.parseAnyAMSUTC(utcTime);
    }

    @Override
    public void loadEnvFromUser(WnUser u) {
        if (null != u && u.hasMeta()) {
            for (Map.Entry<String, Object> en : u.getMeta().entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                this.env.put(Ws.upperCase(key), val);
            }
        }
    }

    @Override
    public NutBean getEnv() {
        return env;
    }

    @Override
    public String getEnvString(String key, String dft) {
        return env.getString(key, dft);
    }

    @Override
    public int getEnvInt(String key, int dft) {
        return env.getInt(key, dft);
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
