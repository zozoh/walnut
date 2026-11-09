package com.site0.walnut.web2.support;

import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;

/**
 * 过滤器需要的最小鉴权能力（包装 {@code WnLoginApi}，便于 JUnit 注入假实现）。
 */
public interface WnAuthLookup {

    /**
     * 按票据取会话（不存在/过期由调用方判断）。
     */
    WnSession getSession(String ticket);

    /**
     * 触碰会话（续期）。
     */
    void touchSession(WnSession session);

    /**
     * 按用户名取用户（不存在返回 <code>null</code>）。
     */
    WnUser checkUser(String name);

}
