package com.site0.walnut.api.auth;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

public class WnAuthEvent extends NutMap {

    public static final String ACCOUNT_CREATED = "account:created";

    public static final String SESSION_CREATED = "session:created";

    public static boolean isMyEvent(String eventName) {
        return ACCOUNT_CREATED.equals(eventName) || SESSION_CREATED.equals(eventName);
    }

    private String name;

    private String domainName;

    private String domainHomePath;

    private WnAccount me;

    private WnAuthSession session;

    public String toString() {
        return String.format("AuthEvent(%s)@%s:U<%s>", name, domainName, me.toString());
    }

    public NutBean toBean() {
        NutMap bean = new NutMap();
        bean.putAll(this);
        bean.put("@name", name);
        bean.put("@domain", domainName);
        bean.put("@home", domainHomePath);
        if (null != me)
            bean.put("@me", me.toBean());
        if (null != session)
            bean.put("@session", session.toMapForClient());
        return bean;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getDomainHomePath() {
        return domainHomePath;
    }

    public void setDomainHomePath(String domainHomePath) {
        this.domainHomePath = domainHomePath;
    }

    public WnAccount getMe() {
        return me;
    }

    public void setMe(WnAccount me) {
        this.me = me;
    }

    public WnAuthSession getSession() {
        return session;
    }

    public void setSession(WnAuthSession session) {
        this.session = session;
    }

}
