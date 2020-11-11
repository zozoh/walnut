package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_umount extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        if (args.length <= 0) {
            throw Er.create("e.cmd.unmount.lackargs", args);
        }
        String ph = Wn.normalizeFullPath(args[0], sys);
        WnObj o = sys.io.check(null, ph);
        WnAccount me = sys.getMe();
        if (!me.isSameName(o.creator())) {
            if (!sys.auth.isMemberOfGroup(me, "root") && !sys.auth.isAdminOfGroup(me, o.group())) {
                sys.err.println("permission denied");
                return;
            }
        }
        sys.io.setMount(o, null);

    }

}
