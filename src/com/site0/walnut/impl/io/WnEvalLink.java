package com.site0.walnut.impl.io;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.AbstractWnSecurity;
import com.site0.walnut.login.session.WnSession;

public class WnEvalLink extends AbstractWnSecurity {

    public WnEvalLink(WnIo io) {
        super(io);
    }

    @Override
    public WnObj access(WnObj nd, boolean asNull) {
        return nd;
    }

    @Override
    public WnObj enter(WnObj nd, boolean asNull) {
        return __eval_obj(nd);
    }

    @Override
    public WnObj read(WnObj nd, boolean asNull) {
        return __eval_obj(nd);
    }

    @Override
    public WnObj write(WnObj nd, boolean asNull) {
        return __eval_obj(nd);
    }

    @Override
    public WnObj meta(WnObj nd, boolean asNull) {
        return nd;
    }

    @Override
    public WnObj remove(WnObj nd, boolean asNull) {
        return nd;
    }

    @Override
    public boolean test(WnObj nd, int mode) {
        return true;
    }

    @Override
    public boolean test(WnObj nd, int mode, WnSession user) {
        return true;
    }

}
