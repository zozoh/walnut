package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Disks;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_find extends JvmExecutor {

    @Override
    public void exec(final WnSystem sys, String[] args) {
        WnObj p = this.getCurrentObj(sys);

        String ph = args[0];

        WnObj o;
        if (".".equals(ph)) {
            o = p;
        } else {
            String path = Wn.normalizeFullPath(ph, sys);
            o = sys.io.check(null, path);
        }

        final String base = p.path();

        sys.io.walk(o, new Callback<WnObj>() {
            public void invoke(WnObj obj) {
                String rph = Disks.getRelativePath(base, obj.path());
                sys.out.writeLine(rph);
            }
        }, WalkMode.DEPTH_NODE_FIRST);

    }

}
