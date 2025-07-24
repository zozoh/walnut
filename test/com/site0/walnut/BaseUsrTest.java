package com.site0.walnut;

import org.nutz.log.Log;

import com.site0.walnut.api.io.WnSecurity;
import com.site0.walnut.impl.io.WnSecurityImpl;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;

public abstract class BaseUsrTest extends BaseIoTest {

    static Log log = Wlog.getTEST();

    protected WnSecurity security;

    protected WnSecurity _old_security;

    @Override
    protected void on_before() {
        log.info("> BaseUsrTest.on_before enter");
        security = new WnSecurityImpl(io, auth);

        // 设置线程上下文的安全检查
        WnContext wc = Wn.WC();
        _old_security = wc.getSecurity();
        wc.setSecurity(security);
        prepareSession(root);
        log.info("> BaseUsrTest.on_before Wn.WC().setSecurity(security);");
    }

    protected void prepareSession(WnUser u) {
        WnSession se = auth.createSession(u, Wn.SET_UNIT_TEST);
        Wn.WC().setSession(se);
    }

    protected void clearSession() {
        WnSession se = Wn.WC().getSession();
        if (null != se) {
            Wn.WC().setSession(null);
            auth.removeSession(se);
        }
    }

    @Override
    protected void on_after() {
        clearSession();
        Wn.WC().setSecurity(_old_security);
        log.info("> BaseUsrTest.on_after");
    }

}
