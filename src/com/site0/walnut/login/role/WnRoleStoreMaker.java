package com.site0.walnut.login.role;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.WnLoginRoleOptions;
import com.site0.walnut.util.Ws;

public class WnRoleStoreMaker {

    public WnRoleStore make(WnIo io, NutBean sessionVars, WnLoginRoleOptions options) {
        WnRoleStore re;
        // 采用 SQL 映射
        if (null != options.sqlQuery) {
            re = new WnSqlRoleStore(io, sessionVars, options);
        }
        // 采用标准存储
        else {
            String roleDirPath = Ws.sBlank(options.path, "~/role");
            re = new WnStdRoleStore(io, sessionVars, roleDirPath, options);
        }
        return new WnRoleStoreProxy(io, re);
    }

}
