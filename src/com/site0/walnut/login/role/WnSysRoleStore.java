package com.site0.walnut.login.role;

import org.nutz.trans.Proton;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.WnRole;
import com.site0.walnut.login.WnRoleList;
import com.site0.walnut.login.WnRoleStore;
import com.site0.walnut.login.WnRoleType;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.util.Wn;

public class WnSysRoleStore implements WnRoleStore {

    private WnIo io;
    private WnRoleStore impl;

    public WnSysRoleStore(WnIo io, WnRoleStore impl) {
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
    public WnRoleList queryRolesOf(String name) {
        return Wn.WC().nosecurity(io, new Proton<WnRoleList>() {
            protected WnRoleList exec() {
                return impl.queryRolesOf(name);
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
    public void removeRole(String uid, String name) {
        Wn.WC().nosecurity(io, () -> {
            impl.removeRole(uid, name);
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
    public synchronized WnRole addRole(String uid, String name, WnRoleType type) {
        return Wn.WC().nosecurity(io, new Proton<WnRole>() {
            protected WnRole exec() {
                return impl.addRole(uid, name, type);
            }
        });
    }

}
