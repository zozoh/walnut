package com.site0.walnut.login.usr;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.UserRace;
import com.site0.walnut.login.WnLoginUserOptions;

public class WnUserStoreMaker {

    private UserRace userRace;

    public WnUserStoreMaker(UserRace userRace) {
        this.userRace = userRace;
    }

    public WnUserStore make(WnIo io,
                            NutBean sessionVars,
                            WnLoginUserOptions options,
                            String domain) {
        WnUserStore re;
        // 采用 SQL 映射
        if (null != options.sqlFetch) {
            re = new WnSqlUserStore(userRace, io, sessionVars, options, domain);
        }
        // 采用标准存储
        else {
            re = new WnStdUserStore(userRace,
                                    io,
                                    sessionVars,
                                    options.path,
                                    options.defaultMeta,
                                    domain);
        }
        return new WnUserStoreProxy(io, re);
    }

}
