package com.site0.walnut.ext.util.react.action;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.api.WnAuthExecutable;
import com.site0.walnut.api.WnOutputable;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.WnSession;

public class ReactActionContext {

    public WnAuthExecutable runner;

    public WnOutputable out;

    public WnIo io;

    public WnSession session;

    public NutBean vars;

    public ReactActionContext() {}

    public ReactActionContext(WnSystem sys, NutBean vars) {
        this.runner = sys;
        this.out = sys.out;
        this.io = sys.io;
        this.session = sys.session;
        this.vars = vars;
    }

}
