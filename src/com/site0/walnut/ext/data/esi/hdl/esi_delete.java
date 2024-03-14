package com.site0.walnut.ext.data.esi.hdl;

import com.site0.walnut.ext.data.esi.EsiConf;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class esi_delete extends esi_xxx {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        EsiConf conf = conf(sys, hc);
        if (conf == null) {
            sys.err.print("e.cmd.esi.mapping.miss_esi_conf");
            return;
        }
        for (String id : hc.params.vals) {
            esi(hc.ioc).delete(sys.getMyName(), conf, id);
        }
    }

}
