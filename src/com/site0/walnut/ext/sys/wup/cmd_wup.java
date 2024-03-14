package com.site0.walnut.ext.sys.wup;

import java.util.Arrays;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class cmd_wup extends JvmHdlExecutor {

    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        if (hc.args.length == 0)
            return;
        if (hc.args.length > 1) {
            hc.hdlName = hc.args[0] + "_" + hc.args[1];
            hc.args = Arrays.copyOfRange(hc.args, 2, hc.args.length);
        } else {
            hc.hdlName = hc.args[0];
            hc.args = Arrays.copyOfRange(hc.args, 1, hc.args.length);
        }
    }
    
    protected void _parse_params(WnSystem sys, JvmHdlContext hc) {
        super._parse_params(sys, hc);

        
        WnObj dataHome = sys.io.fetch(null, Wn.normalizeFullPath(hc.params.get("data", "~/wup"), sys));
        hc.setv("dataHome", dataHome);
    }
}
