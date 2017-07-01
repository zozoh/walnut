package org.nutz.walnut.ext.trash.hdl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class trash_mv extends trash_xxx {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        for (String path : hc.params.vals) {
            path = Wn.normalizeFullPath(path, sys);
            WnObj wobj = sys.io.check(null, path);
            if (!"home".equals(wobj.d0()) || "trash".equals(wobj.d0()) || wobj.d1() == null) {
                sys.err.println("e.cmd.trash.not_allow:" + path);
                return;
            }
            String dst = checkUserTrashRoot(sys).path() + "/" + wobj.name();
            NutMap metas = new NutMap("_path", wobj.path());
            if (sys.io.exists(null, dst)) {
                int count = 1;
                while (true) {
                    String tmp = dst + "_" + count;
                    if (!sys.io.exists(null, tmp)) {
                        dst = tmp;
                        break;
                    }
                }
            }
            sys.out.printlnf("%s -> %s", wobj.path(), dst);
            WnObj newObj = sys.io.move(wobj, dst);
            sys.io.appendMeta(newObj, metas);
        }
    }

}
