package org.nutz.walnut;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.impl.io.WnSecurityImpl;
import org.nutz.walnut.util.Wn;

public abstract class BaseUsrTest extends BaseIoTest {

    protected WnSecurity security;

    @Override
    protected void on_before(PropertiesProxy pp) {
        super.on_before(pp);

        security = new WnSecurityImpl(io, auth);
        // security = new WnSecurityImpl(indexer, tree, usrs);

        // 为了 JUnit 测试，每次都要清空线程的用户缓存
        Wn.WC().clearMe();
    }

}
