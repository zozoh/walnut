package org.nutz.walnut.api.auth;

public interface WnAccountLoader {

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

    WnAccount checkAccountById(String uid);

}
