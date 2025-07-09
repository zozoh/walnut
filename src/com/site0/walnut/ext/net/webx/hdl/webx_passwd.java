package com.site0.walnut.ext.net.webx.hdl;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.net.webx.WebxContext;
import com.site0.walnut.ext.net.webx.WebxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.WnRoleList;
import com.site0.walnut.login.WnSession;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class webx_passwd extends WebxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(strict)$");
    }

    @Override
    protected void process(WnSystem sys, WebxContext fc, ZParams params) {
        String nameOrPhoneOrEmail = params.getString("name");
        String newPassword = params.check("newpass");

        try {
            // 获取用户
            WnUser u = null;

            // TODO 严格模式下，必须需要 name 和 captcha 来验证一下不是机器人

            // 只有管理员才能直接指定用户
            if (!Ws.isBlank(nameOrPhoneOrEmail)) {
                WnUser me = sys.getMe();
                WnRoleList roles = sys.auth.getRoles(me);

                String mainGroup = sys.getMyGroup();
                if (roles.isMemberOfRole("root") || roles.isAdminOfRole(mainGroup)) {
                    String name = params.get("name");
                    u = fc.api.checkUser(name);
                }
                // 否则没有权限
                else {
                    throw Er.create("e.cmd.webx.passwd.NoPvg");
                }
            }
            // 否则必须有票据
            else {
                String ticket = params.val_check(0);
                WnSession se = fc.api.checkSession(ticket);
                u = se.getUser();
            }

            // 再次防空，虽然不太可能
            if (null == u) {
                throw Er.create("e.cmd.webx.passwd.FailToGetUser");
            }

            // 更新密码
            fc.api.changePassword(u, newPassword);

            // 将用户作为结果记录一下表示成功
            fc.result = u;

        }
        catch (Throwable e) {
            fc.error = Er.wrap(e);
        }

    }

}
