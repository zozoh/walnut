package com.site0.walnut.api.auth;

import com.site0.walnut.api.io.WnObj;

public interface WnAuthSetup {

    /**
     * @return 默认的业务角色名
     */
    String getDefaultRoleName();

    /**
     * @return 账户数据所在目录
     */
    WnObj getAccountDir();

    /**
     * 账号对应的角色数据存放的目录，角色对象假想的数据结构为：
     * 
     * <pre>
     * {
     *   nm : "roleName",
     *   roleInDomain: 10,
     *   isdft: false
     * }
     * </pre>
     * 
     * 当然，系统的的账号系统，是没有这个目录的，所以返回的是 NULL，只有域用户体系，才可能会有这个目录
     * 
     * @return 账户数据对应的角色目录。
     */
    WnObj getRoleDir();

    /**
     * @param user
     *            账户对象
     * 
     * @param authCreate
     *            true 表示如果不存在就创建
     * 
     * @return 账户对象对应的头像对象
     */
    WnObj getAvatarObj(WnAccount user, boolean autoCreate);

    /**
     * @return 会话数据所在目录
     */
    WnObj getSessionDir();

    /**
     * @return 默认会话持续时间（秒）
     */
    int getSessionDefaultDuration();

    /**
     * @return 短会话持续时间（秒）
     */
    int getSessionTransientDuration();

    /**
     * @return 验证码服务实例
     */
    WnCaptchaService getCaptchaService();

    /**
     * 账户对象创建后的后续处理, 主要是系统账户需要初始化主目录以及组等操作
     * 
     * @param auth
     *            账户校验接口
     * 
     * @param user
     *            新创建的账户对象
     */
    void afterAccountCreated(WnAuthService auth, WnAccount user);

    /**
     * 账户对象重命名后的后续处理，主要是系统账户需要初始化主目录以及组等操作
     * 
     * @param auth
     *            账户校验接口
     * 
     * @param user
     *            即将被重命名的账户对象
     * 
     * @param newName
     *            要被重命名的新名字
     * 
     * @return true 表示可以继续重命名账户。 false 则不行
     */
    boolean beforeAccountRenamed(WnAuthService auth, WnAccount user, String newName);

    /**
     * 账户对象被删除前的预先处理，要预先做的清理工作，主要是删除系统账户的组以及主目录
     *
     * @param auth
     *            账户校验接口
     * 
     * @param user
     *            即将被删除的账户对象
     * @return true 表示可以继续删除账户。 false 则不行
     */
    boolean beforeAccountDeleted(WnAuthService auth, WnAccount user);

}
