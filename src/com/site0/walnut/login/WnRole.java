package com.site0.walnut.login;

import org.nutz.lang.util.NutBean;

public interface WnRole {
    
    String getId();

    String getName();

    WnRoleType getType();

    boolean isMember();

    boolean isAdmin();

    boolean isMatchName(String name);

    NutBean toBean();

    void setUserId(String userId);

    String getUserId();

    void setUserName(String userName);

    String getUserName();

    boolean hasUserName();

    boolean hasUserId();

}
