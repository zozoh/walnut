package org.nutz.walnut.ext.sys.root;

import java.util.List;

import org.nutz.lang.Stopwatch;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnRun;
import org.nutz.walnut.util.ZParams;

public class cmd_setup extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        final ZParams params = ZParams.parse(args, "v", "^(quiet)$");

        // 得到要操作的帐号
        Wn.WC().security(new WnEvalLink(sys.io), new Atom() {
            @Override
            public void run() {
                __exec_without_security(sys, params);
            }
        });

    }

    private void __exec_without_security(WnSystem sys, ZParams params) {
        String path = params.val_check(0);
        boolean verb = params.is("v");
        boolean quiet = params.is("quiet");
        boolean hasMode = params.has("m") || params.has("om");
        boolean onlyMode = params.has("om");
        // 得到运行器
        WnRun wr = this.ioc.get(WnRun.class);

        // 得到要操作的用户
        WnAccount me = sys.getMe();
        WnAccount u = me;
        if (params.has("u")) {
            u = sys.auth.checkAccount(params.get("u"));
        }

        // 如果操作的用户不是自己，必须是 root 或者 op 组成员才能做
        if (!u.isSame(me)) {
            if (!sys.auth.isMemberOfGroup(me, "root") && !sys.auth.isMemberOfGroup(me, "op")) {
                throw Er.create("e.cmd.setup.nopvg");
            }
        }

        // 为其创建会话
        WnAuthSession se = sys.auth.createSession(u, false);

        try {
            // 开始记时
            Stopwatch sw = Stopwatch.begin();

            // 得到脚本所在目录
            WnObj oSetupHome = sys.io.fetch(null, "/sys/setup");
            WnObj oDir = sys.io.fetch(oSetupHome, path);
            WnObj oMd = null;

            // 检查脚本目录是否存在
            if (null == oDir) {
                if (quiet)
                    return;
                throw Er.create("e.cmd.setup.nofound", path);
            }

            // 检查特殊模式脚本
            if (hasMode) {
                String md = onlyMode ? params.get("om") : params.get("m");
                oMd = sys.io.fetch(oDir, md);
                if (null == oMd || !oMd.isDIR()) {
                    throw Er.create("e.cmd.setup.nofound", path + "/" + md);
                }
            }

            // 是否执行mode
            if (hasMode) {
                if (!onlyMode) {
                    // 父目录脚本
                    __run_dir(sys, oDir, wr, se, verb);
                }
                __run_dir(sys, oMd, wr, se, verb);
            } else {
                // 父目录脚本
                __run_dir(sys, oDir, wr, se, verb);
            }

            // 结束
            sw.stop();
            if (!quiet) {
                sys.out.println("All done in : " + sw.toString());
            }
        }
        // 释放 session
        finally {
            sys.auth.removeSession(se, 0);
        }

    }

    private void __run_dir(WnSystem sys, WnObj oDir, WnRun wr, WnAuthSession se, boolean verb) {
        // 查询出要执行的文件
        List<WnObj> list = wr.io().getChildren(oDir, null);
        for (WnObj o : list) {
            if (!o.isFILE())
                continue;

            // TODO 不可执行，放弃

            // Js 脚本 ...
            if (o.isType("js")) {
                String cmdText = "jsc '" + o.path() + "'";
                if (verb) {
                    sys.out.printlnf(" - %s: %s", o.name(), cmdText);
                }
                wr.exec("cmd_setup", se, cmdText);
            }
            // 其他脚本
            else {
                String cmdText = sys.io.readText(o);
                if (verb) {
                    sys.out.printlnf(" - %s: %s", o.name(), cmdText);
                }
                wr.exec("cmd_setup", se, cmdText);
            }
        }
    }

}
