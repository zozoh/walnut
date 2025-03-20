package com.site0.walnut.ext.net.webx.website.loader;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.ext.net.webx.website.WebxRoleLoader;

public class UniqWebxRoleLoader implements WebxRoleLoader {

    private WnIo io;

    private NutBean sessionVars;

    public UniqWebxRoleLoader(WnIo io, NutBean sessionVars, String roleHomePath) {
        this.io = io;
        this.sessionVars = sessionVars;
    }

}
