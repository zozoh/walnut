package org.nutz.walnut.impl.box.cmd;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

public class cmd_adduser extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        ZParams params = ZParams.parse(args, "cqnN", "^(exists)$");

        // 要添加的用户
        String str = params.val_check(0);
        //WnUsrInfo info = new WnUsrInfo(str);

        // 创建后的用户
        //WnUsr u = sys.usrService.fetchBy(info);
        WnAccount u = sys.auth.getAccount(str);

        // 用户存在 ...
        if (null != u) {
            // 不能忍，跑错
            if (!params.is("exists")) {
                throw Er.create("e.cmd.addusr.exists", str);
            }
        }
        // 不存在，创建吧
        else {
            u = new WnAccount();
            u.setName(str);
            // 分析密码
            if (params.has("p")) {
                String passwd = params.get("p");
                if ("true".equals(passwd)) {
                    passwd = "123456";
                }
                u.setPasswd(passwd);
            }

            // 创建用户
            u = sys.auth.createAccount(u);

            // 初始化设置
            String setup = params.get("setup");
            if (null != setup) {
                // 准备命令
                String cmdText = "setup -u id:" + u.getId() + " '" + setup + "'";

                // 指定了模式
                String md = params.get("m");
                if (null != md) {
                    cmdText += " -m '" + md + "'";
                }

                // 执行设置
                sys.exec2(cmdText);
            }
        }

        // 用 JSON 方式输出
        if (params.has("json")) {
            JsonFormat jfmt = Cmds.gen_json_format(params);
            jfmt.setLocked("^(passwd|salt)$");
            sys.out.print(Json.toJson(u, jfmt));
            if(!params.is("N"))
                sys.out.println();
        }
        // 用模版方式输出
        else if (params.has("out")) {
            String tmpl = params.get("out");
            NutMap bean = u.toBean();
            sys.out.print(Cmds.out_by_tmpl(tmpl, bean));
            if(!params.is("N"))
                sys.out.println();
        }
    }

}
