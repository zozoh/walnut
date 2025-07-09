package com.site0.walnut.login.role;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.login.WnRoleType;

public abstract class WnRoles {

    public static WnRoleType fromInt(int v) {
        switch (v) {
        case 0:
            return WnRoleType.GUEST;
        case 1:
            return WnRoleType.ADMIN;
        case 10:
            return WnRoleType.MEMBER;
        case 100:
            return WnRoleType.CANDIDATE;
        case -1:
            return WnRoleType.BLOCK;
        default:
            throw Er.create("e.invalid.sysRoleValue", v);
        }
    }

}
