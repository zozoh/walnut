package com.site0.walnut.ext.sys.refer;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnReferApi;
import com.site0.walnut.core.mapping.support.MountInfo;
import com.site0.walnut.core.mapping.support.WnVoBMOptions;
import com.site0.walnut.impl.box.JvmFilterContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.role.WnRoleList;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.web.WnConfig;

public class ReferContext extends JvmFilterContext {

    /**
     * 引用服务的类型，默认的相当于 <code>redis</code>
     * 
     */
    // public String referSercieType;

    /**
     * 引用服务的配置文件名，譬如 <code>pet</code> 那么会从
     * <code>~/.redis/pet.io.refer.json</code> 加载引用服务的配置文件。
     * 
     * 如果不声明这个配置，则会加载系统的全局引用服务。 当然，这需要系统管理员权限
     */
    // public String configName;

    /**
     * 将从这个对象处的 "mnt" 字段获取映射信息
     */
    public WnObj oDir;

    public WnReferApi getReferApi(WnSystem sys) {
        // 获取系统引用
        if (null == oDir) {
            // 需要系统管理员权限
            WnUser me = sys.getMe();
            WnRoleList roles = sys.roles().getRoles(me);
            if (!roles.isMemberOfRole("root")) {
                throw Er.create("e.me.nopvg");
            }
            // 获取系统全局引用接口
            return sys.services.getReferApi();
        }
        // 获取自己所在的域
        WnConfig config = sys.services.getConfig();
        String mount = oDir.mount();
        MountInfo mi = new MountInfo(mount);
        WnVoBMOptions opt = new WnVoBMOptions(mi.bm.arg);

        return opt.getReferApi(sys.io, config);
    }

}
