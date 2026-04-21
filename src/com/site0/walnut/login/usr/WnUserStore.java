package com.site0.walnut.login.usr;

import java.util.List;

import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.login.UserRace;

public interface WnUserStore {

    /**
     * 由于我们有三种 WnLoginApi 的实现
     * 
     * - STD： 标准会话存储，标准用户存储，标准角色存储 - DOMAIN： 域SQL会话存储，域SQL用户存储，域SQL角色存储 -
     * HYDRATE: 标准会话存储，域SQL用户存储，域SQL角色存储
     * 
     * 当从系统用户登录到了域用户，我们采用的是 HYDRATE 模式 然后在控制台用户执行 exit
     * 
     * 退回到父亲会话时，我们需要能感知到底用的是哪一个用户存储。 以便用能力切换到正确的用户存储接口
     * 
     * @return 当前实现是否是标准用户库实现
     */
    boolean isStdUserStore();

    UserRace getUserRace();

    void patchDefaultEnv(WnUser u);

    WnUser addUser(WnUser u);

    void saveUserMeta(WnUser u);

    void updateUserName(WnUser u);

    void updateUserPhone(WnUser u);

    void updateUserEmail(WnUser u);

    void updateUserLastLoginAt(WnUser u);

    void updateUserPassword(WnUser u, String rawPassword);

    /**
     * 查询一定数量的账户，如果没有在查询条件里设置限制，默认会查询 100 个
     * 
     * @param q
     *            查询条件
     * @return 结果列表
     */
    List<WnUser> queryUser(WnQuery q);

    /**
     * 获取账户对象
     * 
     * @param q
     *            查询条件
     * @return 账户对象。 null 表示不存在
     */
    WnUser getUser(WnQuery q);

    /**
     * 获取账户对象，如果不存在，抛错
     * 
     * @param q
     *            查询条件
     * @return 账户对象
     * @throws "e.auth.account.noexist"
     *             - 账户不存在
     * @throws "e.auth.account.multiExists"
     *             - 账户存在多条记录
     */
    WnUser checkUser(WnQuery q);

    /**
     * 获取账户对象
     * 
     * @param nameOrPhoneOrEmail
     *            账户登录名或ID或手机号或邮箱
     * @return 账户对象。 null 表示不存在
     */
    WnUser getUser(String nameOrPhoneOrEmail);

    /**
     * 获取账户对象，如果不存在，抛错
     * 
     * @param nameOrPhoneOrEmail
     *            账户登录名或ID或手机号或邮箱
     * @return 账户对象
     * @throws "e.auth.account.noexist"
     *             - 账户不存在
     */
    WnUser checkUser(String nameOrPhoneOrEmail);

    /**
     * 根据信息获取账户对象
     * 
     * @param info
     *            账户对象信息，可以指定部分字段，例如电话，邮箱，OAuth2等
     * @return 账户对象。 null 表示不存在
     */
    WnUser getUser(WnUser info);

    /**
     * 获取账户对象，如果不存在，抛错
     * 
     * @param info
     *            账户对象信息，主要会采用 id|name|phone|email 来查询
     * @return 账户对象
     * @throws "e.auth.account.noexist"
     *             - 账户不存在
     */
    WnUser checkUser(WnUser info);

    WnUser getUserById(String uid);

    WnUser checkUserById(String uid);

}
