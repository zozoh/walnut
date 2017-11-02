package org.nutz.walnut.ext.voucher.hdl;

import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class voucher_init implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        sys.nosecurity(()->sys.io.createIfNoExists(null, "/sys/voucher/"+sys.me.name(), WnRace.DIR));
    }

    
}
