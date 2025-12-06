package com.site0.walnut.web.impl;

import java.util.Map;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.io.WnSecurityImpl;
import com.site0.walnut.impl.srv.WnBoxRunning;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.role.WnRoleList;
import com.site0.walnut.login.role.WnRoleType;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wn;

public class AppCheckAccess {

    private NutMap roles;

    public boolean doCheck(WnIo io, WnSession se, WnLoginApi auth, WnBoxRunning run) {
        WnSecurityImpl secur = new WnSecurityImpl(io, auth);
        // 检查角色
        if (null != roles) {
            for (Map.Entry<String, Object> en : roles.entrySet()) {
                String roleName = en.getKey();
                Object value = en.getValue();
                if (null == value) {
                    continue;
                }
                if (!this.checkRole(roleName, value.toString(), io, se, auth, secur)) {
                    return false;
                }
            }
        }

        // 全部通过检查
        return true;
    }

    private boolean checkRole(String roleName,
                              String path,
                              WnIo io,
                              WnSession se,
                              WnLoginApi auth,
                              WnSecurityImpl secur) {
        String ta_grp;
        // @XXX 直接表示组名
        if (path.startsWith("@")) {
            ta_grp = path.substring(1).trim();
        }
        // 根据路径
        else {
            WnObj oTa = Wn.checkObj(io, se, path);
            // 根据权限码
            if (roleName.matches("^[rwx-]{3}$")) {
                int mode = Wn.Io.modeFromStr(roleName);
                return secur.test(oTa, mode);
            }
            // 根据角色
            ta_grp = oTa.group();
        }
        WnRoleType shouldBeRole = WnRoleType.valueOf(roleName.toUpperCase());
        // 管理员
        WnUser me = se.getUser();
        WnRoleList roles = auth.roleLoader(se).getRoles(me);
        if (WnRoleType.ADMIN == shouldBeRole) {
            return roles.isAdminOfRole(ta_grp);
        }
        // 成员
        if (WnRoleType.MEMBER == shouldBeRole) {
            return roles.isMemberOfRole(ta_grp);
        }
        // 权限写的不对，禁止
        return false;
    }

    public NutMap getRoles() {
        return roles;
    }

    public void setRoles(NutMap roles) {
        this.roles = roles;
    }

}
