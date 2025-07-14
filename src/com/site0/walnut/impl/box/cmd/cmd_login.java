package com.site0.walnut.impl.box.cmd;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.impl.io.WnEvalLink;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.role.WnRoleList;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.site.WnLoginSite;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_login extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        Wn.WC().security(new WnEvalLink(sys.io), () -> {
            __exec_without_security(sys, args);
        });

    }

    private void __exec_without_security(WnSystem sys, String[] args) {
        // 解析参数
        ZParams params = ZParams.parse(args, "cnqH");
        String uname = params.val_check(0);

        WnUser me = sys.getMe();

        // 子站点登录
        String hostName = params.getString("host");
        String siteIdOrPath = params.getString("site");

        // 获取权鉴接口
        WnLoginSite site = WnLoginSite.create(sys.io, siteIdOrPath, hostName);
        WnLoginApi auth = sys.auth;
        // 采用了子站点登录模式
        if (null != site) {
            auth = site.auth();
        }

        // 保存目标用户变量
        WnUser ta = auth.checkUser(uname);
        WnSession newSe;

        // ............................................
        // 开始检查权限了
        WnRoleList myRoles = sys.auth.getRoles(me);
        WnRoleList taRoles = sys.auth.getRoles(ta);

        // 自己不能登录到自己
        if (me.isSame(ta)) {
            throw Er.create("e.cmd.login.self", me.getName());
        }
        // 域管理员可以登录到域的子账号
        if (null != site) {
            if (!myRoles.isAdminOfRole(site.getDomain())) {
                throw Er.create("e.cmd.login.me.forbid", "Need Admin of Domain");
            }
            // 确保【目标账号】可以访问域【目标站点】主目录
            site.assertHomeAccessable(ta);
        }
        // 那就是登录到别的域
        else {
            // 我必须是根管理员
            // 对方必须不能是根管理员
            if (!myRoles.isAdminOfRole("root") || taRoles.isAdminOfRole("root")) {
                throw Er.create("e.cmd.login.me.forbid");
            }
        }

        // 嗯，可以登录，获取登录时长
        int du = auth.getSessionDuration();

        // 全部检查没问题，可以创建新会话了
        newSe = auth.createSession(sys.session, ta, du);

        // 输出这个新会话
        JsonFormat jfmt = Cmds.gen_json_format(params);
        NutMap bean = newSe.toBean();
        String json = Json.toJson(bean, jfmt);
        sys.out.println(json);

        // ............................................
        // 在沙盒的上下文标记一把
        sys.attrs().put(Wn.MACRO.CHANGE_SESSION, Wlang.map("seid", newSe.getTicket()));
    }

}
