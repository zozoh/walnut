package org.nutz.walnut.api.auth;

import java.util.List;

import org.nutz.walnut.api.io.WnQuery;

public interface WnAccountLoader {

    /**
     * 查询一定数量的账户，如果没有在查询条件里设置限制，默认会查询 100 个
     * 
     * @param q
     *            查询条件
     * @return 结果列表
     */
    List<WnAccount> queryAccount(WnQuery q);

    /**
     * 获取账户对象
     * 
     * @param nameOrPhoneOrEmail
     *            账户登录名或ID或手机号或邮箱
     * @return 账户对象。 null 表示不存在
     */
    WnAccount getAccount(String nameOrPhoneOrEmail);

    /**
     * 获取账户对象，如果不存在，抛错
     * 
     * @param nameOrPhoneOrEmail
     *            账户登录名或ID或手机号或邮箱
     * @return 账户对象
     * @throws "e.auth.account.noexist"
     *             - 账户不存在
     */
    WnAccount checkAccount(String nameOrPhoneOrEmail);

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

    WnAccount getAccountById(String uid);

    WnAccount checkAccountById(String uid);

}
