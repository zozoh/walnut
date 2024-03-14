package com.site0.walnut.ext.sys.trash.hdl;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class trash_recover extends trash_xxx {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        for (String path : hc.params.vals) {
            path = Wn.normalizeFullPath(path, sys);
            WnObj wobj = sys.io.check(null, path);
            if (!"trash".equals(wobj.d0()) || !sys.getMe().isSameName(wobj.d1())) {
                sys.err.println("e.cmd.trash.not_allow");
                return;
            }
            String originPath = wobj.getString("_path");
            if (originPath == null) {
                sys.err.printlnf("!!Origin Path for %s NOT FOUND!!", originPath);
                return;
            }
            if (sys.io.exists(null, originPath)) {
                sys.err.println("!!Origin Path Exist!! " + originPath);
                return;
            }
            sys.out.printlnf("%s -> %s", wobj.path(), originPath);
            sys.io.move(wobj, originPath);
        }
    }

}
