package com.site0.walnut;

import com.site0.walnut.api.io.WnSecurity;
import com.site0.walnut.impl.io.WnSecurityImpl;
import com.site0.walnut.util.Wn;

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
