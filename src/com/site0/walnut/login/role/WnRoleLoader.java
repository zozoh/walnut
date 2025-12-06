package com.site0.walnut.login.role;

import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.login.usr.WnUserStore;

public class WnRoleLoader {

    private WnRoleStore roles;

    private WnUserStore users;

    public WnRoleLoader(WnRoleStore roles, WnUserStore users) {
        this.roles = roles;
        this.users = users;
    }

    public WnRoleList getRoles(WnUser u) {
        WnRoleList list = roles.getRoles(u);
        for (WnRole r : list) {
            if (!r.hasUserName()) {
                r.setUserName(u.getName());
            }
        }
        return list;
    }

    public WnRoleList queryRolesOf(String name) {
        WnRoleList list = roles.queryRolesOf(name);
        for (WnRole r : list) {
            if (!r.hasUserName()) {
                WnUser u = users.getUserById(r.getUserId());
                if (null != u) {
                    r.setUserName(u.getName());
                }
            }
        }
        return list;
    }

    public WnRoleType getRoleTypeOfGroup(WnUser u, String group) {
        WnRoleList list = roles.getRoles(u);
        return list.getRoleTypeOfGroup(group);
    }

    public WnRoleType getRoleTypeOfMainGroup(WnUser u) {
        String group = u.getMainGroup();
        WnRoleList list = roles.getRoles(u);
        return list.getRoleTypeOfGroup(group);
    }
}
