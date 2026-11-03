package com.site0.walnut.web.module.app;

import com.site0.walnut.login.usr.WnUser;

public class WamOcmInfo {

    /**
     * 配置文件的所属用户目录，是 <code>/root</code>
     * 还是 <code>/home/xxx</code>
     */
    private String confHomePath;

    /**
     * 执行登录，验证码，OTP 相关操作的用户 可以从它获得 email 等信息
     */
    private WnUser user;

    /**
     * 本次操作涉及的 domain
     */
    private String domain;

    /**
     * 本次操作涉及的 account
     */
    private String account;

    public String getConfHomePath() {
        return confHomePath;
    }

    public void setConfHomePath(String confHomePath) {
        this.confHomePath = confHomePath;
    }

    public WnUser getUser() {
        return user;
    }

    public void setUser(WnUser user) {
        this.user = user;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

}
