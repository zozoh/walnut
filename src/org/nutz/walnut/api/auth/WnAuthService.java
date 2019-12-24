package org.nutz.walnut.api.auth;

import org.nutz.lang.util.NutMap;

public interface WnAuthService extends WnGroupRoleService, WnAccountLoader {

    /**
     * 创建一个新账户
     * 
     * @param user
     *            账户对象
     * @return 创建后的账户对象
     */
    WnAccount createAccount(WnAccount user);

    /**
     * 持久化账户的信息（包括元数据集）
     * 
     * @param user
     *            账户对象
     */
    void saveAccount(WnAccount user);

    /**
     * 持久化账户的信息字段（不包括元数据集）
     * 
     * @param user
     *            账户对象
     * 
     * @param mode
     *            保存的模式。位串，定义在 WnAuths.ABMM
     * 
     * @see WnAuths.ABMM#LOGIN
     * @see WnAuths.ABMM#INFO
     * @see WnAuths.ABMM#PASSWD
     * @see WnAuths.ABMM#WXOPEN
     * @see WnAuths.ABMM#META
     * @see WnAuths.ABMM#ALL
     */
    void saveAccount(WnAccount user, int mode);

    /**
     * 持久化账户的信息字段（不包括元数据集）
     * 
     * @param user
     *            账户对象。它也会通过 WnAccount.updateBy 函数自动被传入元数据更新
     * 
     * @param meta
     *            要保存的元数据
     * 
     * @return 更新后的账户（新对象）
     */
    WnAccount saveAccount(WnAccount user, NutMap meta);

    /**
     * 将指定账户修改为新名称
     * 
     * @param user
     *            账户对象
     * @param newName
     *            新名称（登录名）
     */
    void renameAccount(WnAccount user, String newName);

    void deleteAccount(WnAccount user);

    /**
     * 根据会话票据，找回自身。执行次操作将会自动更新票据
     * 
     * @param ticket
     *            票据
     * @return 更新后的会话对象, null 表示找不到
     * @throws "e.auth.account.noexist"
     *             : 会话对应用户不存在
     * @throws "e.auth.account.invalid"
     *             : 会话对应用户非法
     */
    WnAuthSession getSession(String ticket);

    WnAuthSession getSession(String byType, String byValue);

    /**
     * 根据会话票据，找回自身。如果不存在则抛错。
     * 
     * @param ticket
     *            票据
     * @return 会话对象
     * @throws "e.auth.ticket.noexist"
     *             : 票据找不到对应会话
     * @throws "e.auth.account.noexist"
     *             : 会话对应用户不存在
     * @throws "e.auth.account.invalid"
     *             : 会话对应用户非法
     */
    WnAuthSession checkSession(String ticket);

    WnAuthSession checkSession(String byType, String byValue);

    /**
     * 根据会话票据，找回自身。如果不存在则抛错。<br>
     * 执行本次操作将会自动更新票据
     * 
     * @param se
     *            会话对象
     * @return 更新后的会话对象
     * @throws "e.auth.ticket.noexist"
     *             : 票据找不到对应会话
     * @throws "e.auth.account.noexist"
     *             : 会话对应用户不存在
     * @throws "e.auth.account.invalid"
     *             : 会话对应用户非法
     */
    WnAuthSession touchSession(WnAuthSession se);

    /**
     * 根据指定用户，创建一个顶级会话
     * 
     * @param user
     *            账户对象
     * @param longSession
     *            是创建长会话还是短会话.true 表示长会话
     * @return 新创建的会话对象
     */
    WnAuthSession createSession(WnAccount user, boolean longSession);

    /**
     * 为一个会话创建子会话(一定是长会话)
     * 
     * @param pse
     *            父会话对象
     * @param user
     *            账户对象
     * @return 自会话对象
     */
    WnAuthSession createSession(WnAuthSession pse, WnAccount user);

    /**
     * 持久化会话的信息字段和变量集
     * 
     * @param se
     *            会话对象
     */
    void saveSession(WnAuthSession se);

    /**
     * 持久化会话的信息字段（不包括变量集）
     * 
     * @param se
     *            会话对象
     */
    void saveSessionInfo(WnAuthSession se);

    /**
     * 持久化会话的变量集
     * 
     * @param se
     *            会话对象
     */
    void saveSessionVars(WnAuthSession se);

    /**
     * 注销当前会话，并返回当前会话的父会话
     * 
     * @param se
     *            会话对象
     * @param delay
     *            延迟多少毫秒移除（这个是一个大概的值，因为要等清理进程回收） 0 表示立即删除。
     *            如果是负数，则清理进程一启动就会删除，如果是正数，清理进程会判断是否大于这个设定的过期时间
     * @return 被注销的会话对象的父会话，null 表示给入的是顶级会话
     */
    WnAuthSession removeSession(WnAuthSession se, long delay);

    /**
     * 用微信的权限码自动登录
     * 
     * @param code
     *            微信的权限码
     * 
     * @param wxCodeType
     *            微信票据代码类型: mp(小程序) gh(公众号)，默认为 gh
     * 
     * @return 登录成功后的会话
     */
    WnAuthSession loginByWxCode(String code, String wxCodeType);

    /**
     * 绑定手机/邮箱
     * 
     * @param nameOrIdOrPhoneOrEmail
     *            账号（手机号|邮箱）
     * @param scene
     *            验证码场景
     * @param vcode
     *            验证码
     * @param ticket
     *            用户登录的票据（必须是已经登录的用户才能绑定啊）
     * @return 绑定成功后的会话（可能会切换会话）
     * 
     * @throws "e.auth.login.noexists"
     *             : 用户不存在
     * @throws "e.auth.invalid.captcha"
     *             : 验证码错误
     */
    WnAuthSession bindAccount(String nameOrIdOrPhoneOrEmail,
                              String scene,
                              String vcode,
                              String ticket);

    /**
     * 验证码登录
     * 
     * @param phoneOrEmail
     *            账号（手机号|邮箱）
     * @param scene
     *            验证码场景
     * @param vcode
     *            验证码
     * @return 登录成功后的会话
     * 
     * @throws "e.auth.login.noexists"
     *             : 用户不存在
     * @throws "e.auth.invalid.captcha"
     *             : 验证码错误
     */
    WnAuthSession loginByVcode(String phoneOrEmail, String scene, String vcode);

    /**
     * 用户名（手机·邮箱）密码登录
     * 
     * @param nameOrIdOrPhoneOrEmail
     *            账号（手机号|邮箱|登录名）
     * @param passwd
     *            密码（明文）
     * @return 登录成功后的会话
     * 
     * @throws "e.auth.login.noexists"
     *             : 用户不存在
     * @throws "e.auth.login.invalid.passwd"
     *             : 用户名密码错误
     * @throws "e.auth.login.forbid"
     *             : 没声明密码，因此禁止此种登录形式
     */
    WnAuthSession loginByPasswd(String nameOrIdOrPhoneOrEmail, String passwd);

    /**
     * 注销当前会话，并返回当前会话的父会话
     * 
     * @param ticket
     *            用户登录的票据（必须是已经登录的用户才能绑定啊）
     * @param delay
     *            延迟多少毫秒移除（这个是一个大概的值，因为要等清理进程回收） 0 表示立即删除。
     *            如果是负数，则清理进程一启动就会删除，如果是正数，清理进程会判断是否大于这个设定的过期时间
     * @return 被注销的会话对象的父会话，null 表示给入的是顶级会话
     * @throws "e.auth.ticket.noexist"
     *             : 票据找不到对应会话
     */
    WnAuthSession logout(String ticket, long delay);

}
