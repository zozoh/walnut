package org.nutz.walnut;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsrService;

public abstract class BaseUsrTest extends BaseIoTest {

    protected WnUsrService usrs;

    protected WnSessionService ses;

    @Override
    protected void on_before(PropertiesProxy pp) {
        super.on_before(pp);

        usrs = _create_usr_service(pp);
        ses = _create_session_service(pp);
    }

    protected abstract WnSessionService _create_session_service(PropertiesProxy pp);

    protected abstract WnUsrService _create_usr_service(PropertiesProxy pp);

}
