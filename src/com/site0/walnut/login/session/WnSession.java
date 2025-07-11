package com.site0.walnut.login.session;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.login.usr.WnUser;

public interface WnSession {
    
    boolean isSame(WnSession se);

    boolean isSameTicket(String ticket);

    String getTicket();

    boolean isExpired();

    long getExpiAt();

    void setExpiAt(long expiAt);

    String getExpiAtInUTC();

    void setExpiAtInUTC(Object utcTime);

    long getCreateTime();

    void setCreateTime(long createTime);

    String getCreateTimeInUTC();

    void setCreateTimeInUTC(Object utcTime);

    long getLastModified();

    void setLastModified(long lastModified);

    String getLastModifiedInUTC();

    void setLastModifiedInUTC(Object utcTime);

    WnUser getUser();

    NutBean getEnv();

    String getEnvAsStr();

    void setEnv(NutBean env);

    void updateEnv(String key, Object val);

    void updateEnv(NutBean delta);

    WnSession clone();

    String getMyId();

    String getMyName();

    NutMap toBean();

    String getMyGroup();

    void setParentTicket(String parentTicket);

    String getParentTicket();

    boolean hasParentTicket();

    void loadEnvFromUser(WnUser u);

}
