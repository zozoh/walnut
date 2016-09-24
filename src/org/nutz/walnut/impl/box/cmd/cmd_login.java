package org.nutz.walnut.impl.box.cmd;

import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;
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
        WnUsrService us = sys.usrService;

        // 解析参数
        ZParams params = ZParams.parse(args, "cnqH");

        // 得到用户
        WnUsr me = sys.me;
        WnUsr ta = us.check(params.val_check(0));

        // ............................................
        // 开始检查权限了

        // 自己不能登录到自己
        if (me.isSameId(ta))
            throw Er.create("e.cmd.login.self", me.name());

        // root 组管理员能登录到除了 root 组管理员之外任何账户
        if (us.isAdminOfGroup(me, "root") && !us.isAdminOfGroup(ta, "root")) {
            // 嗯，可以登录
        }
        // 否则执行操作的用户必须为 root|op 组成员
        // 目标用户必须不能为 root|op 组成员
        else {
            if (!us.isMemberOfGroup(me, "op") && !us.isMemberOfGroup(me, "root")) {
                throw Er.create("e.cmd.login.me.forbid");
            }
            if (us.isMemberOfGroup(ta, "op") || us.isMemberOfGroup(ta, "root")) {
                throw Er.create("e.cmd.login.ta.forbidden");
            }
        }

        // ............................................
        // 执行 & 得到一个新会话，这个新会话的持续时间为 10 秒
        WnSession newSe = sys.sessionService.create(sys.se, ta);

        // 输出这个新会话
        JsonFormat jfmt = Cmds.gen_json_format(params);
        String json = newSe.toJson(jfmt);
        sys.out.println(json);

        // ............................................
        // 在沙盒的上下文标记一把
        sys.attrs().put(Wn.MACRO.CHANGE_SESSION, Lang.map("seid", newSe.id()));
    }

}
