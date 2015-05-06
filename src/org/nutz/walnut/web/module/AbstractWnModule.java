package org.nutz.walnut.web.module;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.util.Wn;
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

    @Inject("refer:boxService")
    protected WnBoxService boxes;

    @Inject("refer:mimes")
    protected MimeMap mimes;

    protected WnObj _find_app_home(String appName) {
        String rpath = appName.replace('.', '/');
        String appPaths = Wn.WC().checkSE().envs().getString("APP_PATH");
        String[] bases = Strings.splitIgnoreBlank(appPaths, ":");
        for (String base : bases) {
            String ph = Wn.appendPath(base, rpath);
            WnObj o = io.fetch(null, ph);
            if (null != o)
                return o;
        }
        return null;
    }

    protected WnObj _check_app_home(String appName) {
        WnObj oAppHome = _find_app_home(appName);
        if (null == oAppHome)
            throw Er.create("e.app.noexists", appName);
        return oAppHome;
    }
}
