package com.site0.walnut.login.session;

import java.util.List;

import com.site0.walnut.login.usr.WnUserStore;

public interface WnSessionStore {

    WnSession getSession(String ticket, WnUserStore users);

    WnSession getSessionByUserIdAndType(String uid, String type, WnUserStore users);

    WnSession getSessionByUserNameAndType(String unm, String type, WnUserStore users);
    
    List<WnSession> querySession(int limit, WnUserStore users);

    void addSession(WnSession se);

    void saveSessionEnv(WnSession se);

    void saveSessionChildTicket(WnSession se);

    void touchSession(WnSession se, int duInSec);

    /**
     * 移除当前会话，如果会话有父会话，那么就返回父会话，否则返回 null
     * 
     * @param se
     *            会话对象
     * @return 父会话对象或 null
     */
    WnSession reomveSession(WnSession se, WnUserStore users);

    void patchDefaultEnv(WnSession se);

}