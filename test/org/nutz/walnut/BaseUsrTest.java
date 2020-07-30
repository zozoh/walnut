package org.nutz.walnut;

import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.impl.io.WnSecurityImpl;
import org.nutz.walnut.util.Wn;

public abstract class BaseUsrTest extends BaseIoTest {

    protected WnSecurity security;

    @Override
    protected void on_before() {
        security = new WnSecurityImpl(io, auth);

        // 设置线程上下文的安全检查
        Wn.WC().setSecurity(security);
    }

    @Override
    protected void on_after() {}

}
