package com.site0.walnut.login.maker;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.UserRace;
import com.site0.walnut.login.WnLoginUserOptions;
import com.site0.walnut.login.WnUserStore;
import com.site0.walnut.login.usr.WnSqlUserStore;
import com.site0.walnut.login.usr.WnStdUserStore;
import com.site0.walnut.login.usr.WnUserStoreSetup;
import com.site0.walnut.util.Ws;

public class WnUserStoreMaker {

    private UserRace userRace;

    public WnUserStoreMaker(UserRace userRace) {
        this.userRace = userRace;
    }

    public WnUserStore make(WnIo io, NutBean sessionVars, WnLoginUserOptions options) {
        WnUserStoreSetup setup = new WnUserStoreSetup(userRace);
        setup.io = io;
        setup.sessionVars = sessionVars;
        setup.defaultMeta = options.defaultMeta;

        // 采用 SQL 映射
        if (null != options.sqlFetch) {
            setup.daoName = options.daoName;
            setup.sqlHome = Ws.sBlank(options.sqlHome, "~/.sqlx");
            setup.sqlQuery = options.sqlQuery;
            setup.sqlFetch = options.sqlFetch;
            setup.sqlUpdate = options.sqlUpdate;
            setup.sqlInsert = options.sqlInsert;
            return new WnSqlUserStore(setup);
        }
        // 采用标准存储
        setup.path = Ws.sBlank(options.path, "~/.domain/session");
        return new WnStdUserStore(setup);
    }

}
