package org.nutz.walnut.web.module;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.web.WnConfig;

public abstract class AbstractWnModule {

    @Inject("refer:conf")
    protected WnConfig conf;

    @Inject("refer:io")
    protected WnIo io;

    @Inject("refer:sessionService")
    protected WnSessionService sess;

    @Inject("refer:usrService")
    protected WnUsrService usrs;

    @Inject("refer:processService")
    protected WnBoxService boxes;

    @Inject("refer:mime")
    protected MimeMap mime;

}
