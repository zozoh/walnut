package com.site0.walnut.login;

public interface WnSessionStore {

    WnSession getSession(String ticket, WnUserStore users);

    void addSession(WnSession se);

    void saveSessionEnv(WnSession se);

    void touchSession(WnSession se, long sessionDuration);
    
    boolean reomveSession(WnSession se);

}