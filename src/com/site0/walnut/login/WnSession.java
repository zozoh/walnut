package com.site0.walnut.login;

import org.nutz.lang.util.NutBean;

public interface WnSession {

    WnUser getUser();

    String getTicket();

    String getExpiAtInUTC();

    long getExpiAt();

    boolean isExpired();

    void setExpiAt(long expiAt);

    NutBean getEnv();

    String getEnvAsStr();

    void setEnv(NutBean env);

    void updateEnv(String key, Object val);

    void updateEnv(NutBean delta);

}
