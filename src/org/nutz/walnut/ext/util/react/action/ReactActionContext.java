package org.nutz.walnut.ext.util.react.action;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.WnAuthExecutable;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.impl.box.WnSystem;

public class ReactActionContext {

    public WnAuthExecutable runner;

    public WnOutputable out;

    public WnIo io;

    public WnAuthSession session;

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
