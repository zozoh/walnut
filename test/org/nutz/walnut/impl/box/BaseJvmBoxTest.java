package org.nutz.walnut.impl.box;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Lang;
import org.nutz.walnut.Wnts;
import org.nutz.walnut.api.box.AbstractWnBoxTest;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsrService;

public abstract class BaseJvmBoxTest extends AbstractWnBoxTest {

    @Override
    protected WnBoxService _create_box_service(PropertiesProxy pp) {
        JvmExecutorFactory jef = new JvmExecutorFactory();
        jef.scanPkgs = Lang.array("org.nutz.walnut.impl.box.cmd");

        return new JvmBoxService(jef);
    }

    @Override
    protected WnUsrService _create_usr_service(PropertiesProxy pp) {
        return Wnts.create_io_usr_service(pp, io);
    }

    @Override
    protected WnSessionService _create_session_service(PropertiesProxy pp) {
        return Wnts.create_io_session_service(pp, io, usrs);
    }

}
