package org.nutz.walnut.impl.auth;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;

public class WnAuthSysSetup extends AbstractWnAuthSetup {

    public WnAuthSysSetup(WnIo io) {
        super(io);
    }

    @Override
    public String getDefaultRoleName() {
        return "admin";
    }

    @Override
    protected WnObj createOrFetchAccountDir() {
        String aph = "/sys/usr/";
        return io.createIfNoExists(null, aph, WnRace.DIR);
    }

    @Override
    protected WnObj createOrFetchSessionDir() {
        String aph = "/var/session/";
        return io.createIfNoExists(null, aph, WnRace.DIR);
    }

    @Override
    protected WnObj createOrFetchCaptchaDir() {
        String aph = "/var/captcha/";
        return io.createIfNoExists(null, aph, WnRace.DIR);
    }

    @Override
    public long getSessionDefaultDuration() {
        return this.getConfLong("se-sys-du", 3600);
    }

    @Override
    protected WnObj getWeixinConf() {
        throw Lang.noImplement();
    }

    @Override
    public void afterAccountCreated(WnAccount user) {
        // 为用户创建组
        
        // 设置组管理员
        
        // 为用户创建主目录
        
        // 更新用户元数据，设置主目录，OPEN 等
    }

    @Override
    public void afterAccountRenamed(WnAccount user) {
        // 重命名用户所在组
    }

    @Override
    public boolean beforeAccountDeleted(WnAccount user) {
        // 删除账户所在的组
        return true;
    }

}
