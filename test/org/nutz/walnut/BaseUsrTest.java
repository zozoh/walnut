package org.nutz.walnut;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Mirror;
import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.impl.io.WnSecurityImpl;
import org.nutz.walnut.impl.usr.IoWnSessionService;
import org.nutz.walnut.impl.usr.IoWnUsrService;

public abstract class BaseUsrTest extends BaseIoTest {

    protected WnUsrService usrs;

    protected WnSessionService ses;

    protected WnSecurity security;

    protected WnUsr root;

    private WnUsrService _create_io_usr_service() {
        IoWnUsrService usrs = new IoWnUsrService();
        Mirror.me(usrs).setValue(usrs, "io", io);
        Mirror.me(usrs).setValue(usrs, "regexName", pp.get("usr-name"));
        Mirror.me(usrs).setValue(usrs, "regexPhone", pp.get("usr-phone"));
        Mirror.me(usrs).setValue(usrs, "regexEmail", pp.get("usr-email"));
        Mirror.me(usrs).setValue(usrs, "usrHome", pp.get("usr-home", "/sys/usr"));
        Mirror.me(usrs).setValue(usrs, "grpHome", pp.get("grp-home", "/sys/grp"));
        usrs.on_create();
        return usrs;
    }

    private WnSessionService _create_io_session_service() {
        IoWnSessionService ses = new IoWnSessionService();
        Mirror.me(ses).setValue(ses, "io", io);
        Mirror.me(ses).setValue(ses, "usrs", usrs);
        Mirror.me(ses).setValue(ses, "duration", pp.getInt("se-duration"));
        Mirror.me(ses).setValue(ses, "sessionHome", pp.get("se-home", "/sys/session"));
        ses.on_create();
        return ses;
    }

    @Override
    protected void on_before(PropertiesProxy pp) {
        super.on_before(pp);

        usrs = _create_io_usr_service();
        ses = _create_io_session_service();
        security = new WnSecurityImpl(io, usrs);
        // security = new WnSecurityImpl(indexer, tree, usrs);

        root = usrs.create("root", "123456");
    }

}
