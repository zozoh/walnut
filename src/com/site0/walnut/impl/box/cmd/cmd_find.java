package com.site0.walnut.impl.box.cmd;

import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Disks;
import com.site0.walnut.api.io.WalkMode;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_find extends JvmExecutor {

    @Override
    public void exec(final WnSystem sys, String[] args) {
        WnObj p = sys.getCurrentObj();
        ZParams params = ZParams.parse(args, null);

        String ph = params.vals.length > 0 ? params.vals[0] : ".";

        WnObj o;
        if (".".equals(ph)) {
            o = p;
        } else {
            String path = Wn.normalizeFullPath(ph, sys);
            o = sys.io.check(null, path);
        }

        final String base;
        if (params.is("p")) {
            base = o.getRegularPath();
        } else {
            base = p.getRegularPath();
        }

        sys.io.walk(o, new Callback<WnObj>() {
            public void invoke(WnObj obj) {
                String ph = obj.getRegularPath();
                String rph = Disks.getRelativePath(base, ph);
                sys.out.println(rph);
            }
        }, WalkMode.DEPTH_NODE_FIRST);

    }

}
