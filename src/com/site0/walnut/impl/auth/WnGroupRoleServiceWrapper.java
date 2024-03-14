package com.site0.walnut.impl.auth;

import java.util.List;

import org.nutz.trans.Proton;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnGroupAccount;
import com.site0.walnut.api.auth.WnGroupRole;
import com.site0.walnut.api.auth.WnGroupRoleService;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Wn;

public class WnGroupRoleServiceWrapper implements WnGroupRoleService {

    private WnGroupRoleServiceImpl impl;

    private WnAccount root;

    @Override
    public WnObj getSysRoleDir() {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnObj>() {
            protected WnObj exec() {
                return impl.getSysRoleDir();
            }
        });
    }

    public WnGroupRoleServiceWrapper(WnIo io, WnAccount root) {
        this.impl = new WnGroupRoleServiceImpl(io);
        this.root = root;
    }

    @Override
    public WnGroupRole getGroupRole(WnAccount user, String groupName) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnGroupRole>() {
            protected WnGroupRole exec() {
                return impl.getGroupRole(user, groupName);
            }
        });
    }

    @Override
    public void setGroupRole(WnAccount user, String groupName, WnGroupRole role) {
        Wn.WC().suCoreNoSecurity(impl.io, root, () -> {
            impl.setGroupRole(user, groupName, role);
        });
    }

    @Override
    public WnGroupRole removeGroupRole(WnAccount user, String groupName) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnGroupRole>() {
            protected WnGroupRole exec() {
                return impl.removeGroupRole(user, groupName);
            }
        });
    }

    @Override
    public List<WnGroupAccount> getAccounts(String groupName) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<List<WnGroupAccount>>() {
            protected List<WnGroupAccount> exec() {
                return impl.getAccounts(groupName);
            }
        });
    }

    @Override
    public List<WnGroupAccount> getGroups(WnAccount user) {
        return Wn.WC().nosecurity(impl.io, new Proton<List<WnGroupAccount>>() {
            protected List<WnGroupAccount> exec() {
                return impl.getGroups(user);
            }
        });
    }

    @Override
    public boolean isRoleOfGroup(WnGroupRole role, WnAccount user, String... groupNames) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<Boolean>() {
            protected Boolean exec() {
                return impl.isRoleOfGroup(role, user, groupNames);
            }
        });
    }

    @Override
    public boolean isAdminOfGroup(WnAccount user, String... groupNames) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<Boolean>() {
            protected Boolean exec() {
                return impl.isAdminOfGroup(user, groupNames);
            }
        });
    }

    @Override
    public boolean isMemberOfGroup(WnAccount user, String... groupNames) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<Boolean>() {
            protected Boolean exec() {
                return impl.isMemberOfGroup(user, groupNames);
            }
        });
    }

}
