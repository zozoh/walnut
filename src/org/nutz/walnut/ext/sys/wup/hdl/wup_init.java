package org.nutz.walnut.ext.sys.wup.hdl;

import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class wup_init implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        String mode = hc.params.get("mode", "0");
        String cmdText = "app-init -file org/nutz/walnut/ext/wup/hdl/wup_" + mode + ".init ~";
        sys.exec(cmdText);
    }

}
