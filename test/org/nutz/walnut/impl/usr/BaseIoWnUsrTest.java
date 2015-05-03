package org.nutz.walnut.impl.usr;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.walnut.Wnts;
import org.nutz.walnut.api.usr.AbstractWnUsrTest;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsrService;

public abstract class BaseIoWnUsrTest extends AbstractWnUsrTest {

    protected WnUsrService _create_usr_service(PropertiesProxy pp) {
        return Wnts.create_io_usr_service(pp, io);
    }

    protected WnSessionService _create_session_service(PropertiesProxy pp) {
        return Wnts.create_io_session_service(pp, io, usrs);
    }

}
