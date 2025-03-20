package com.site0.walnut.ext.net.webx.website.loader;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.ext.net.webx.website.WebxAccountLoader;

public class UniqWebxAccountLoader implements WebxAccountLoader {

    private WnIo io;

    private NutBean sessionVars;

    public UniqWebxAccountLoader(WnIo io,
                                 NutBean sessionVars,
                                 String accountHomePath,
                                 NutBean dftEnv) {
        this.io = io;
        this.sessionVars = sessionVars;
    }

}
