package org.nutz.walnut.api.auth;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;

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
     * @return 会话数据所在目录
     */
    WnObj getSessionDir();

    /**
     * @return 默认会话持续时间（秒）
     */
    long getSessionDefaultDuration();

    /**
     * @return 短会话持续时间（秒）
     */
    long getSessionTransientDuration();

    /**
     * @return 微信公众号后台操作接口
     */
    WnIoWeixinApi getWeixinApi();

    /**
     * @return 验证码服务实例
     */
    WnCaptchaService getCaptchaService();

    /**
     * 账户对象创建后的后续处理, 主要是系统账户需要初始化主目录以及组等操作
     * 
     * @param user
     *            新创建的账户对象
     */
    void afterAccountCreated(WnAccount user);

    /**
     * 账户对象重命名后的后续处理，主要是系统账户需要初始化主目录以及组等操作
     * 
     * @param user
     *            已经被重命名的账户对象
     */
    void afterAccountRenamed(WnAccount user);

    /**
     * 账户对象被删除前的预先处理，要预先做的清理工作，主要是删除系统账户的组以及主目录
     * 
     * @param user
     *            即将被删除的账户对象
     * @return true 表示可以继续删除账户。 false 则不行
     */
    boolean beforeAccountDeleted(WnAccount user);

}
