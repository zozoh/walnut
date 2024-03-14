package com.site0.walnut.web.impl;

import java.util.Map;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthService;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.auth.WnGroupRole;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.io.WnSecurityImpl;
import com.site0.walnut.impl.srv.WnBoxRunning;
import com.site0.walnut.util.Wn;

public class AppCheckAccess {

    private NutMap roles;

    public boolean doCheck(WnIo io, WnAuthSession se, WnAuthService auth, WnBoxRunning run) {
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
                              WnAuthSession se,
                              WnAuthService auth,
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
        WnGroupRole shouldBeRole = WnGroupRole.valueOf(roleName);
        // 管理员
        WnAccount me = se.getMe();
        if (WnGroupRole.ADMIN == shouldBeRole) {
            return auth.isAdminOfGroup(me, ta_grp);
        }
        // 成员
        if (WnGroupRole.MEMBER == shouldBeRole) {
            return auth.isMemberOfGroup(me, ta_grp);
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
