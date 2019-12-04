package org.nutz.walnut.api.auth;

public interface WnAuthService {

    /**
     * 获取账户对象
     * 
     * @param nameOrIdOrPhoneOrEmail
     *            账户登录名或ID或手机号或邮箱
     * @return 账户对象。 null 表示不存在
     */
    WnAccount getAccount(String nameOrIdOrPhoneOrEmail);

    /**
     * 获取账户对象，如果不存在，抛错
     * 
     * @param nameOrIdOrPhoneOrEmail
     *            账户登录名或ID或手机号或邮箱
     * @return 账户对象
     * @throws "e.auth.account.noexist"
     *             - 账户不存在
     */
    WnAccount checkAccount(String nameOrIdOrPhoneOrEmail);

    /**
     * 根据信息获取账户对象
     * 
     * @param info
     *            账户对象信息，可以指定部分字段，例如电话，邮箱，OAuth2等
     * @return 账户对象。 null 表示不存在
     */
    WnAccount getAccount(WnAccount info);

    /**
     * 获取账户对象，如果不存在，抛错
     * 
     * @param info
     *            账户对象信息，可以指定部分字段，例如电话，邮箱，OAuth2等
     * @return 账户对象
     * @throws "e.auth.account.noexist"
     *             - 账户不存在
     */
    WnAccount checkAccount(WnAccount info);

    /**
     * 创建一个新账户
     * 
     * @param user
     *            账户对象
     * @return 创建后的账户对象
     */
    WnAccount createAccount(WnAccount user);

    /**
     * 持久化账户对象的元数据集
     * 
     * @param user
     *            账户对象
     */
    void saveAccountMeta(WnAccount user);

    /**
     * 持久化账户对象的密码（加盐）
     * 
     * @param user
     *            账户对象
     */
    void saveAccountPasswd(WnAccount user);

    /**
     * 将指定账户修改为新名称
     * 
     * @param user
     *            账户对象
     * @param newName
     *            新名称（登录名）
     */
    void renameAccount(WnAccount user, String newName);

    /**
     * 获取某账户对象在指定的系统组中的权限
     * 
     * @param user
     *            账户对象
     * @param groupName
     *            系统的组名
     * @return 账户对象在指定组中的权限。默认为 <code>GUEST</code>
     * @see WnGroupRole
     */
    WnGroupRole getGroupRole(WnAccount user, String groupName);

    /**
     * 判断某账户在给定的组中是否至少有一个符合指定的角色
     * 
     * @param role
     *            指定的角色
     * @param user
     *            账户对象
     * @param groupNames
     *            组名列表
     * @return 如果在任何一个给定组中是管理员返回 true，否则返回 false
     */
    boolean isRoleOfGroup(WnGroupRole role, WnAccount user, String... groupNames);

    /**
     * 判断某账户在给定的组中是否至少有一个是管理员
     * 
     * @param user
     *            账户对象
     * @param groupNames
     *            组名列表
     * @return 如果在任何一个给定组中是管理员返回 true，否则返回 false
     */
    boolean isAdminOfGroup(WnAccount user, String... groupNames);

    /**
     * 判断某账户在给定的组中是否至少有一个是成员
     * 
     * @param user
     *            账户对象
     * @param groupNames
     *            组名列表
     * @return 如果在任何一个给定组中是成员返回 true，否则返回 false
     */
    boolean isMemberOfGroup(WnAccount user, String... groupNames);

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

    /**
     * 根据会话票据，找回自身。如果不存在则抛错。<br>
     * 执行本次操作将会自动更新票据
     * 
     * @param ticket
     *            票据
     * @return 更新后的会话对象
     * @throws "e.auth.ticket.noexist"
     *             : 票据找不到对应会话
     * @throws "e.auth.account.noexist"
     *             : 会话对应用户不存在
     * @throws "e.auth.account.invalid"
     *             : 会话对应用户非法
     */
    WnAuthSession touchSession(String ticket);

    /**
     * 根据指定用户，创建一个顶级会话
     * 
     * @param user
     *            账户对象
     * @return 新创建的会话对象
     */
    WnAuthSession createSession(WnAccount user);

    /**
     * 为一个会话创建子会话
     * 
     * @param pse
     *            父会话对象
     * @param user
     *            账户对象
     * @return 自会话对象
     */
    WnAuthSession createSession(WnAuthSession pse, WnAccount user);

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
     * @param sess
     *            会话对象
     * @return 被注销的会话对象的父会话，null 表示给入的是顶级会话
     */
    WnAuthSession removeSession(WnAuthSession sess);

    /**
     * 用微信的权限码自动登录
     * 
     * @param code
     *            微信的权限码
     * @return 登录成功后的会话
     */
    WnAuthSession loginByWxCode(String code);

    /**
     * 绑定手机/邮箱
     * 
     * @param account
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
    WnAuthSession bindAccount(String account, String scene, String vcode, String ticket);

    /**
     * 验证码登录
     * 
     * @param account
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
    WnAuthSession loginByVcode(String account, String scene, String vcode);

    /**
     * 用户名（手机·邮箱）密码登录
     * 
     * @param account
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
    WnAuthSession loginByPasswd(String account, String passwd);

    /**
     * 注销当前会话，并返回当前会话的父会话
     * 
     * @param ticket
     *            用户登录的票据（必须是已经登录的用户才能绑定啊）
     * @return 被注销的会话对象的父会话，null 表示给入的是顶级会话
     * @throws "e.auth.ticket.noexist"
     *             : 票据找不到对应会话
     */
    WnAuthSession logout(String ticket);

}
