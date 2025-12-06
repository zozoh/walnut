package com.site0.walnut.impl.box.cmd;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.util.Wlang;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.impl.io.WnEvalLink;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.WnLoginApiMaker;
import com.site0.walnut.login.WnLoginOptions;
import com.site0.walnut.login.role.WnRoleList;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.site.WnLoginSite;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
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
        JsonFormat jfmt = Cmds.gen_json_format(params);

        try {
            NutMap bean = _do_login(sys, params);
            // 输出这个新会话
            AjaxReturn re = Ajax.ok().setData(bean);
            String json = Json.toJson(re, jfmt);
            sys.out.println(json);

            // ............................................
            // 在沙盒的上下文标记一把
            String ticket = bean.getString("ticket");
            NutMap macro = Wlang.map("seid", ticket);
            sys.attrs().put(Wn.MACRO.CHANGE_SESSION, macro);
        }
        catch (Exception e) {
            AjaxReturn re = Ajax.fail(e);
            String json = Json.toJson(re, jfmt);
            sys.out.println(json);
        }
    }

    protected NutMap _do_login(WnSystem sys, ZParams params) {
        String uname = params.val_check(0);

        WnUser me = sys.getMe();

        // 子站点登录
        String hostName = params.getString("host");
        String siteIdOrPath = params.getString("site");

        if (!Ws.isBlank(siteIdOrPath)) {
            siteIdOrPath = Wn.normalizeFullPath(siteIdOrPath, sys);
        }

        // 获取权鉴接口
        WnLoginSite site = WnLoginSite.create(sys.io, siteIdOrPath, hostName);
        WnLoginApi auth = sys.auth;
        // 采用了子站点登录模式
        if (null != site) {
            NutBean env = sys.session.getEnv();
            WnLoginOptions options = site.getOptions();
            auth = WnLoginApiMaker.forHydrate().make(sys.io, env, options);
        }

        // 保存目标用户变量
        WnUser ta = auth.checkUser(uname);
        WnSession newSe;

        // ............................................
        // 开始检查权限了
        WnRoleList myRoles = sys.roles().getRoles(me);
        WnRoleList taRoles = sys.roles().getRoles(ta);

        // 自己不能登录到自己
        if (me.isSame(ta)) {
            throw Er.create("e.cmd.login.self", me.getName());
        }
        // 域管理员可以登录到域的子账号
        if (null != site) {
            if (!myRoles.isAdminOfRole(site.getDomain())) {
                throw Er.create("e.cmd.login.me.forbid", "Need Admin of Domain");
            }
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
        newSe = auth.createSession(sys.session, ta, Wn.SET_LOGIN_CMD, du);

        // 确保【目标账号】可以访问域【目标站点】主目录
        if (null != site) {
            site.assertHomeAccessable(newSe);
        }

        NutMap bean = newSe.toBean(sys.auth);
        return bean;
    }

}
