package org.nutz.walnut.api.auth;

public interface WnAuthService {
    
    WnAccount getAccount(String name);
    
    WnAccount checkAccount(String name);

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
     * 根据会话票据，找回自身。执行次操作将会自动更新票据
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
    WnAuthSession checkSession(String ticket);

    /**
     * 注销会话，移除票据
     * 
     * @param ticket
     *            票据
     * @return 更新后的会话对象
     * @throws "e.auth.ticket.noexist"
     *             : 票据找不到对应会话
     */
    WnAuthSession removeSession(String ticket);

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
     * @param ticket
     *            用户登录的票据（必须是已经登录的用户才能绑定啊）
     * @return 被注销的会话对象
     */
    WnAuthSession logout(String ticket);

}
