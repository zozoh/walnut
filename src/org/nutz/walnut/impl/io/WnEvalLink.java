package org.nutz.walnut.impl.io;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.AbstractWnSecurity;

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

}
