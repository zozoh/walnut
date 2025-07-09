package com.site0.walnut.login.session;

import org.nutz.trans.Proton;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.WnSession;
import com.site0.walnut.login.WnSessionStore;
import com.site0.walnut.login.WnUserStore;
import com.site0.walnut.util.Wn;

public class WnSysSessionStore implements WnSessionStore {

    private WnIo io;
    private WnSessionStore impl;

    public WnSysSessionStore(WnIo io, WnSessionStore impl) {
        this.io = io;
        this.impl = impl;
    }

    @Override
    public WnSession getSession(String ticket, WnUserStore users) {
        return Wn.WC().nosecurity(io, new Proton<WnSession>() {
            protected WnSession exec() {
                return impl.getSession(ticket, users);
            }
        });
    }

    @Override
    public void addSession(WnSession se) {
        Wn.WC().nosecurity(io, () -> {
            impl.addSession(se);
        });
    }

    @Override
    public void saveSessionEnv(WnSession se) {
        Wn.WC().nosecurity(io, () -> {
            impl.saveSessionEnv(se);
        });
    }

    @Override
    public void touchSession(WnSession se, long sessionDuration) {
        Wn.WC().nosecurity(io, () -> {
            impl.touchSession(se, sessionDuration);
        });
    }

    @Override
    public WnSession reomveSession(WnSession se, WnUserStore users) {
        return Wn.WC().nosecurity(io, new Proton<WnSession>() {
            protected WnSession exec() {
                return impl.reomveSession(se, users);
            }
        });
    }

}
