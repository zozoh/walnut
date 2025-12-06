package com.site0.walnut.login.session;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;

public class WnSessionStoreMaker {

    public WnSessionStore make(WnIo io, NutBean sessionVars, WnLoginSessionOptions options) {
        WnSessionStore re;
        // 采用 SQL 映射
        if (null != options.sqlFetch) {
            re = new WnSqlSessionStore(io, sessionVars, options);
        }
        // 采用标准存储
        else {
            re = new WnStdSessionStore(io, sessionVars, options.path, options.defaultEnv);
        }
        return new WnSessionStoreProxy(io, re);
    }

}
