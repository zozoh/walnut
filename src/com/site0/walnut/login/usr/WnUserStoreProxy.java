package com.site0.walnut.login.usr;

import java.util.List;

import org.nutz.trans.Proton;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.login.UserRace;
import com.site0.walnut.util.Wn;

public class WnUserStoreProxy implements WnUserStore {

    // It will be injected by Ioc
    private WnIo io;
    private WnUserStore impl;

    public WnUserStoreProxy(WnIo io, WnUserStore impl) {
        this.io = io;
        this.impl = impl;
    }

    public UserRace getUserRace() {
        return impl.getUserRace();
    }

    public void patchDefaultEnv(WnUser u) {
        impl.patchDefaultEnv(u);
    }

    public WnUser addUser(WnUser u) {
        return Wn.WC().nosecurity(io, new Proton<WnUser>() {
            protected WnUser exec() {
                return impl.addUser(u);
            }
        });
    }

    public void saveUserMeta(WnUser u) {
        Wn.WC().nosecurity(io, () -> {
            impl.saveUserMeta(u);
        });
    }

    public void updateUserName(WnUser u) {
        Wn.WC().nosecurity(io, () -> {
            impl.updateUserName(u);
        });
    }

    public void updateUserPhone(WnUser u) {
        Wn.WC().nosecurity(io, () -> {
            impl.updateUserPhone(u);
        });
    }

    public void updateUserEmail(WnUser u) {
        Wn.WC().nosecurity(io, () -> {
            impl.updateUserEmail(u);
        });
    }

    public void updateUserLastLoginAt(WnUser u) {
        Wn.WC().nosecurity(io, () -> {
            impl.updateUserLastLoginAt(u);
        });
    }

    public void updateUserPassword(WnUser u, String rawPassword) {
        Wn.WC().nosecurity(io, () -> {
            impl.updateUserPassword(u, rawPassword);
        });
    }

    public List<WnUser> queryUser(WnQuery q) {
        return Wn.WC().nosecurity(io, new Proton<List<WnUser>>() {
            protected List<WnUser> exec() {
                return impl.queryUser(q);
            }
        });
    }

    public WnUser getUser(WnQuery q) {
        return Wn.WC().nosecurity(io, new Proton<WnUser>() {
            protected WnUser exec() {
                return impl.getUser(q);
            }
        });
    }

    public WnUser checkUser(WnQuery q) {
        return Wn.WC().nosecurity(io, new Proton<WnUser>() {
            protected WnUser exec() {
                return impl.checkUser(q);
            }
        });
    }

    public WnUser getUser(String nameOrPhoneOrEmail) {
        return Wn.WC().nosecurity(io, new Proton<WnUser>() {
            protected WnUser exec() {
                return impl.getUser(nameOrPhoneOrEmail);
            }
        });
    }

    public WnUser checkUser(String nameOrPhoneOrEmail) {
        return Wn.WC().nosecurity(io, new Proton<WnUser>() {
            protected WnUser exec() {
                return impl.checkUser(nameOrPhoneOrEmail);
            }
        });
    }

    public WnUser getUser(WnUser info) {
        return Wn.WC().nosecurity(io, new Proton<WnUser>() {
            protected WnUser exec() {
                return impl.getUser(info);
            }
        });
    }

    public WnUser checkUser(WnUser info) {
        return Wn.WC().nosecurity(io, new Proton<WnUser>() {
            protected WnUser exec() {
                return impl.checkUser(info);
            }
        });
    }

    public WnUser getUserById(String uid) {
        return Wn.WC().nosecurity(io, new Proton<WnUser>() {
            protected WnUser exec() {
                return impl.getUserById(uid);
            }
        });
    }

    public WnUser checkUserById(String uid) {
        return Wn.WC().nosecurity(io, new Proton<WnUser>() {
            protected WnUser exec() {
                return impl.checkUserById(uid);
            }
        });
    }

}
