package com.site0.walnut.login.session;

import java.util.List;

import org.nutz.trans.Proton;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.login.usr.WnUserStore;
import com.site0.walnut.util.Wn;

public class WnSessionStoreProxy implements WnSessionStore {

    private WnIo io;
    private WnSessionStore impl;

    public WnSessionStoreProxy(WnIo io, WnSessionStore impl) {
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
    public WnSession getSessionByUserIdAndType(String uid, String type, WnUserStore users) {
        return Wn.WC().nosecurity(io, new Proton<WnSession>() {
            protected WnSession exec() {
                return impl.getSessionByUserIdAndType(uid, type, users);
            }
        });
    }

    @Override
    public WnSession getSessionByUserNameAndType(String unm, String type, WnUserStore users) {
        return Wn.WC().nosecurity(io, new Proton<WnSession>() {
            protected WnSession exec() {
                return impl.getSessionByUserNameAndType(unm, type, users);
            }
        });
    }

    @Override
    public List<WnSession> querySession(WnQuery q, WnUserStore users) {
        return Wn.WC().nosecurity(io, new Proton<List<WnSession>>() {
            protected List<WnSession> exec() {
                return impl.querySession(q, users);
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
    public void saveSessionChildTicket(WnSession se) {
        Wn.WC().nosecurity(io, () -> {
            impl.saveSessionChildTicket(se);
        });
    }

    @Override
    public void touchSession(WnSession se, int seInSec) {
        Wn.WC().nosecurity(io, () -> {
            impl.touchSession(se, seInSec);
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

    @Override
    public void patchDefaultEnv(WnSession se) {
        // TODO Auto-generated method stub

    }

}
