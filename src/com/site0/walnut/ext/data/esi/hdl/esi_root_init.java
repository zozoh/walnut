package com.site0.walnut.ext.data.esi.hdl;

import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class esi_root_init implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        if (!sys.getMe().isRoot()) {
            sys.err.print("only for root");
            return;
        }
        sys.io.createIfNoExists(null, "/sys/hook/write/esi", WnRace.FILE);
        sys.io.createIfNoExists(null, "/sys/hook/meta/esi", WnRace.FILE);
        sys.io.createIfNoExists(null, "/sys/hook/create/esi", WnRace.FILE);
        sys.io.createIfNoExists(null, "/sys/hook/delete/esi", WnRace.FILE);
        sys.out.println("done");
    }

}
