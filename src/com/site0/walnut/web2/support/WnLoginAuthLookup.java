package com.site0.walnut.web2.support;

import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;

/**
 * {@link WnAuthLookup} 的正式实现：薄包装 {@code WnLoginApi}。
 */
public class WnLoginAuthLookup implements WnAuthLookup {

    private final WnLoginApi auth;

    public WnLoginAuthLookup(WnLoginApi auth) {
        this.auth = auth;
    }

    @Override
    public WnSession getSession(String ticket) {
        if (null == auth || null == ticket)
            return null;
        return auth.getSession(ticket);
    }

    @Override
    public void touchSession(WnSession session) {
        if (null != auth && null != session)
            auth.touchSession(session);
    }

    @Override
    public WnUser checkUser(String name) {
        if (null == auth)
            return null;
        return auth.checkUser(name);
    }

}
