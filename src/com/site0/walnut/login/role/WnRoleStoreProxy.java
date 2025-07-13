package com.site0.walnut.login.role;

import org.nutz.trans.Proton;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wn;

public class WnRoleStoreProxy implements WnRoleStore {

    private WnIo io;
    private WnRoleStore impl;

    public WnRoleStoreProxy(WnIo io, WnRoleStore impl) {
        this.io = io;
        this.impl = impl;
    }

    @Override
    public WnRoleList getRoles(WnUser u) {
        return Wn.WC().nosecurity(io, new Proton<WnRoleList>() {
            protected WnRoleList exec() {
                return impl.getRoles(u);
            }
        });
    }

    @Override
    public WnRoleList queryRolesOf(String grp) {
        return Wn.WC().nosecurity(io, new Proton<WnRoleList>() {
            protected WnRoleList exec() {
                return impl.queryRolesOf(grp);
            }
        });
    }

    @Override
    public void removeRole(WnRole role) {
        Wn.WC().nosecurity(io, () -> {
            impl.removeRole(role);
        });
    }

    @Override
    public void removeRole(String uid, String grp) {
        Wn.WC().nosecurity(io, () -> {
            impl.removeRole(uid, grp);
        });
    }

    @Override
    public void clearCache() {
        impl.clearCache();
    }

    @Override
    public synchronized WnRoleList getRoles(String uid) {
        return Wn.WC().nosecurity(io, new Proton<WnRoleList>() {
            protected WnRoleList exec() {
                return impl.getRoles(uid);
            }
        });
    }

    @Override
    public synchronized WnRole addRole(String uid, String grp, WnRoleType type, String unm) {
        return Wn.WC().nosecurity(io, new Proton<WnRole>() {
            protected WnRole exec() {
                return impl.addRole(uid, grp, type, unm);
            }
        });
    }

    @Override
    public synchronized WnRole setRole(String uid, String grp, WnRoleType type, String unm) {
        return Wn.WC().nosecurity(io, new Proton<WnRole>() {
            protected WnRole exec() {
                return impl.setRole(uid, grp, type, unm);
            }
        });
    }

}
