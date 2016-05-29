package org.nutz.walnut;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Mirror;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.impl.usr.IoWnSessionService;
import org.nutz.walnut.impl.usr.IoWnUsrService;

public class Wnts {

    public static WnUsrService create_io_usr_service(PropertiesProxy pp, WnIo io) {
        IoWnUsrService usrs = new IoWnUsrService();
        Mirror.me(usrs).setValue(usrs, "io", io);
        // Mirror.me(usrs).setValue(usrs, "regexName", pp.get("usr-name"));
        // Mirror.me(usrs).setValue(usrs, "regexPhone", pp.get("usr-phone"));
        // Mirror.me(usrs).setValue(usrs, "regexEmail", pp.get("usr-email"));
        // usrs.on_create();
        return usrs;
    }

    public static WnSessionService create_io_session_service(PropertiesProxy pp,
                                                             WnIo io,
                                                             WnUsrService usrs) {
        IoWnSessionService ses = new IoWnSessionService();
        Mirror.me(ses).setValue(ses, "io", io);
        Mirror.me(ses).setValue(ses, "usrs", usrs);
        Mirror.me(ses).setValue(ses, "duration", pp.getInt("se-duration"));
        ses.on_create();
        return ses;
    }

}
