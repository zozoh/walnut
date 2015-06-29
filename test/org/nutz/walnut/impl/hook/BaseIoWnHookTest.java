package org.nutz.walnut.impl.hook;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.walnut.Wnts;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.hook.AbstractWnHookTest;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.impl.box.JvmBoxService;
import org.nutz.walnut.impl.box.JvmExecutorFactory;

public abstract class BaseIoWnHookTest extends AbstractWnHookTest {

    @Override
    protected WnBoxService _create_box_service(PropertiesProxy pp) {
        JvmExecutorFactory jef = new JvmExecutorFactory();
        Mirror.me(jef).setValue(jef, "scanPkgs", Lang.array("org.nutz.walnut.impl.box.cmd"));

        return new JvmBoxService(jef);
    }

    protected WnUsrService _create_usr_service(PropertiesProxy pp) {
        return Wnts.create_io_usr_service(pp, io);
    }

    protected WnSessionService _create_session_service(PropertiesProxy pp) {
        return Wnts.create_io_session_service(pp, io, usrs);
    }

}
