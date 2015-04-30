package org.nutz.walnut.impl.usr;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Mirror;
import org.nutz.walnut.api.usr.AbstractWnUsrTest;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsrService;

public abstract class AbstractIoWnUsrTest extends AbstractWnUsrTest {

    protected WnSessionService _create_session_service(PropertiesProxy pp) {
        IoWnSessionService ses = new IoWnSessionService();
        Mirror.me(ses).setValue(ses, "io", io);
        Mirror.me(ses).setValue(ses, "usrs", usrs);
        Mirror.me(ses).setValue(ses, "duration", pp.getInt("se-duration"));
        ses.on_create();
        return ses;
    }

    protected WnUsrService _create_usr_service(PropertiesProxy pp) {
        IoWnUsrService usrs = new IoWnUsrService();
        Mirror.me(usrs).setValue(usrs, "io", io);
        Mirror.me(usrs).setValue(usrs, "regexName", pp.get("usr-name"));
        Mirror.me(usrs).setValue(usrs, "regexPhone", pp.get("usr-phone"));
        Mirror.me(usrs).setValue(usrs, "regexEmail", pp.get("usr-email"));
        usrs.on_create();
        return usrs;
    }

}
