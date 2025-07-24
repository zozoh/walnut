package com.site0.walnut.login.session;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.role.WnRoleLoader;
import com.site0.walnut.login.usr.WnUser;

public interface WnSession {

    /**
     * @return 会话是否由某个域站点创建
     */
    boolean hasSite();

    /**
     * @return 会话对应站点全路径. 如果不指定明，那么就是系统默认的用户、角色存储策略
     * 
     */
    String getSite();

    void setSite(String site);

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

    NutMap toBean(WnLoginApi auth);

    NutMap toBean(WnRoleLoader rl);

    String getMyGroup();

    void setParentTicket(String parentTicket);

    String getParentTicket();

    boolean hasParentTicket();

    void loadEnvFromUser(WnUser u);

    int getEnvInt(String key, int dft);

    String getEnvString(String key, String dft);

    boolean isParentOf(WnSession se);

    void setChildTicket(String childTicket);

    String getChildTicket();

    boolean hasChildTicket();

    void setDuration(int duration);

    int getDuration();

    long getDurationInMs();

    void setType(String type);

    String getType();

    boolean hasType();

}
