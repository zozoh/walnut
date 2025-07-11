package com.site0.walnut.impl.box.cmd;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.role.WnRoleList;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wn;

public class cmd_umount extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        if (args.length <= 0) {
            throw Er.create("e.cmd.unmount.lackargs", args);
        }
        String ph = Wn.normalizeFullPath(args[0], sys);
        WnObj o = sys.io.check(null, ph);
        WnUser me = sys.getMe();
        WnRoleList roles = sys.auth.getRoles(me);
        if (!me.isSameName(o.creator())) {
            if (!roles.isMemberOfRole("root") && !roles.isAdminOfRole(o.group())) {
                sys.err.println("permission denied");
                return;
            }
        }
        sys.io.setMount(o, null);
    }

}
