package com.site0.walnut.login.maker;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.WnLoginSessionOptions;
import com.site0.walnut.login.WnSessionStore;
import com.site0.walnut.login.session.WnSqlSessionStore;
import com.site0.walnut.login.session.WnStdSessionStore;
import com.site0.walnut.login.usr.WnSessionStoreSetup;
import com.site0.walnut.util.Ws;

public class WnSessionStoreMaker {

    public WnSessionStore make(WnIo io, NutBean sessionVars, WnLoginSessionOptions options) {
        WnSessionStoreSetup setup = new WnSessionStoreSetup();
        setup.io = io;
        setup.sessionVars = sessionVars;
        setup.defaultEnv = options.defaultEnv;

        // 采用 SQL 映射
        if (null != options.sqlFetch) {
            setup.daoName = options.daoName;
            setup.sqlHome = Ws.sBlank(options.sqlHome, "~/.sqlx");
            setup.sqlFetch = options.sqlFetch;
            setup.sqlDelete = options.sqlDelete;
            setup.sqlUpdate = options.sqlUpdate;
            setup.sqlInsert = options.sqlInsert;
            return new WnSqlSessionStore(setup);
        }
        // 采用标准存储
        setup.path = Ws.sBlank(options.path, "~/.domain/session");
        return new WnStdSessionStore(setup);
    }

}
