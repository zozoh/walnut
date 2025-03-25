package com.site0.walnut.login.usr;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.UserRace;
import com.site0.walnut.login.WnLoginUserOptions;

public class WnUserStoreSetup extends WnLoginUserOptions {
    public UserRace userRace;
    public WnIo io;
    public NutBean sessionVars;

    /**
     * 默认用户元数据
     */
    public NutBean defaultMeta;

    public WnUserStoreSetup(UserRace userRace) {
        this.userRace = userRace;
    }
}
