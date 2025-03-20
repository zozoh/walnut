package com.site0.walnut.ext.net.webx.website;

import com.site0.walnut.api.io.WnObj;

public class WnWebx {

    /**
     * 站点宿主对象
     * 
     * <ul>
     * <li><code>FILE</code> 当做 JSON 读取信息
     * <li><code>DIR</code> 采用元数据读取信息
     * </ul>
     * 
     * 站点需要从宿主对象读取下面的信息
     * 
     * <pre>
     * {
     *    // 指定站点账号目录，如果想用数据表
     *    // 可以用 ":{daoName?}:t_user"
     *    accounts : "~/user",
     *    
     *    // 指定站点的角色目录，如果想用数据表
     *    // 可以用 ":{daoName?}:t_role"
     *    roles    : "~/roles",
     *    env : {
     *        "!HOME": "/home/${domain}/",
     *        "THEME": "light",
     *        "LANG": "zh-cn",
     *        "TIMEZONE": "GMT+8",
     *        "ENABLE_CONSOLE": "yes"
     *    },
     * }
     * </pre>
     */
    private WnObj oHome;

    /**
     * 账号读取接口【选】
     */
    private WebxAccountLoader accounts;

    /**
     * 角色读取接口【选】
     */
    private WebxRoleLoader roles;

    public WnObj getoHome() {
        return oHome;
    }

    public void setoHome(WnObj oHome) {
        this.oHome = oHome;
    }

    public WebxAccountLoader getAccounts() {
        return accounts;
    }

    public void setAccounts(WebxAccountLoader accounts) {
        this.accounts = accounts;
    }

    public WebxRoleLoader getRoles() {
        return roles;
    }

    public void setRoles(WebxRoleLoader roles) {
        this.roles = roles;
    }

}
