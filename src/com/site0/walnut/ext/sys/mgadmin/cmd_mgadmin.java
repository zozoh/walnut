package com.site0.walnut.ext.sys.mgadmin;

import org.nutz.trans.Atom;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.impl.box.JvmHdlExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.WnRoleList;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.util.Wn;

public class cmd_mgadmin extends JvmHdlExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        // 检查权限: root 组管理员才能操作
        sys.nosecurity(new Atom() {
            public void run() {
                WnUser me = Wn.WC().getMe();
                WnRoleList roles = sys.auth.getRoles(me);
                if (!roles.isMemberOfRole("root")) {
                    throw Er.create("e.cmd.mgadmin.only_for_root_admin");
                }
            }
        });

        // 执行子命令
        super.exec(sys, args);
    }
}
