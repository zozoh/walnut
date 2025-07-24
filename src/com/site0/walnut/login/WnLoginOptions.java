package com.site0.walnut.login;

import com.site0.walnut.login.session.WnLoginSessionOptions;

public class WnLoginOptions {

    public WnLoginSessionOptions session;

    public WnLoginUserOptions user;

    public WnLoginRoleOptions role;

    public String domain;

    /**
     * 【选】采用域账号登录，需要指明站点目录
     */
    public String site;

    /**
     * 标准会话的时长（秒）
     */
    public int sessionDuration;

    /**
     * 临时会话的时长（秒）
     */
    public int sessionShortDu;

    public String wechatMpOpenIdKey;

    public String wechatGhOpenIdKey;

}
