package com.site0.walnut.impl.box.cmd;

import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_deluser extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        ZParams params = ZParams.parse(args, "cqnN", "^(quiet)$");

        // 要删除的用户
        String str = params.val_check(0);
        WnAccount u = sys.auth.getAccount(str);

        // 用户不存在 ...
        if (null == u) {
            throw Er.create("e.cmd.delusr.noexists", str);
        }

        // 执行删除
        sys.auth.deleteAccount(u);

        // 输出
        if (!params.is("quiet")) {
            sys.out.printlnf("user [%s] has been removed", u);
        }
    }

}
