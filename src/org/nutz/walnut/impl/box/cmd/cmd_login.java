package org.nutz.walnut.impl.box.cmd;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

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

        // 得到用户
        WnAccount me = sys.getMe();
        WnAccount ta = sys.auth.checkAccount(params.val_check(0));

        // ............................................
        // 开始检查权限了

        // 自己不能登录到自己
        if (me.isSame(ta))
            throw Er.create("e.cmd.login.self", me.getName());

        // root 用户可以登录到任何用户
        if (me.isRoot()) {
            // 嗯，可以登录
        }
        // root 组管理员能登录到除了 root 组管理员之外任何账户
        else if (sys.auth.isAdminOfGroup(me, "root") && !sys.auth.isAdminOfGroup(ta, "root")) {
            // 嗯，可以登录
        }
        // 否则执行操作的用户必须为 root|op 组成员
        // 目标用户必须不能为 root|op 组成员
        else {
            if (!sys.auth.isMemberOfGroup(me, "root", "op")) {
                throw Er.create("e.cmd.login.me.forbid");
            }
            if (sys.auth.isMemberOfGroup(ta, "root", "op")) {
                throw Er.create("e.cmd.login.ta.forbid");
            }
        }

        // ............................................
        // 执行 & 得到一个新会话
        WnAuthSession newSe = sys.auth.createSession(sys.session, ta, 0);

        // 输出这个新会话
        JsonFormat jfmt = Cmds.gen_json_format(params);
        NutMap bean = newSe.toMapForClient();
        String json = Json.toJson(bean, jfmt);
        sys.out.println(json);

        // ............................................
        // 在沙盒的上下文标记一把
        sys.attrs().put(Wn.MACRO.CHANGE_SESSION, Lang.map("seid", newSe.getTicket()));
    }

}
