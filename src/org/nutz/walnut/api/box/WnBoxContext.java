package org.nutz.walnut.api.box;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;

public class WnBoxContext {

    public WnUsr me;

    public WnSession session;

    public WnIo io;

    public WnSessionService sessionService;

    public WnUsrService usrService;

}
