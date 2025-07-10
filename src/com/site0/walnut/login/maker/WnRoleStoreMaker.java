package com.site0.walnut.login.maker;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.WnLoginRoleOptions;
import com.site0.walnut.login.WnRoleStore;
import com.site0.walnut.login.role.WnSqlRoleStore;
import com.site0.walnut.login.role.WnStdRoleStore;
import com.site0.walnut.login.usr.WnSessionStoreSetup;
import com.site0.walnut.util.Ws;

public class WnRoleStoreMaker {

    public WnRoleStore make(WnIo io, NutBean sessionVars, WnLoginRoleOptions options) {
        WnSessionStoreSetup setup = new WnSessionStoreSetup();
        setup.io = io;
        setup.sessionVars = sessionVars;

        // 采用 SQL 映射
        if (null != options.sqlQuery) {
            return new WnSqlRoleStore(io, sessionVars, options);
        }
        // 采用标准存储
        String roleDirPath = Ws.sBlank(options.path, "~/role");
        return new WnStdRoleStore(io, sessionVars, roleDirPath);
    }

}
