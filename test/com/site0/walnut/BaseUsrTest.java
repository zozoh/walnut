package com.site0.walnut;

import org.nutz.log.Log;

import com.site0.walnut.api.io.WnSecurity;
import com.site0.walnut.impl.io.WnSecurityImpl;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;

public abstract class BaseUsrTest extends BaseIoTest {
    
    static Log log = Wlog.getTEST();

    protected WnSecurity security;

    @Override
    protected void on_before() {
        log.info("> BaseUsrTest.on_before enter");
        security = new WnSecurityImpl(io, auth);

        // 设置线程上下文的安全检查
        Wn.WC().setSecurity(security);
        log.info("> BaseUsrTest.on_before Wn.WC().setSecurity(security);");
    }

    @Override
    protected void on_after() {
        log.info("> BaseUsrTest.on_after");
    }

}
