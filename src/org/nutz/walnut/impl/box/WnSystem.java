package org.nutz.walnut.impl.box;

import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;

public class WnSystem {

    public String original;

    public WnUsr me;

    public WnSession se;

    public JvmBoxInput in;

    public JvmBoxOutput out;

    public JvmBoxOutput err;

    public WnIo io;

    public WnSessionService sessionService;

    public WnUsrService usrService;

    public WnBox box;

}
