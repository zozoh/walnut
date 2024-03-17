package com.site0.walnut.ext.sys.wup.hdl;

import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class wup_init implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        String mode = hc.params.get("mode", "0");
        String cmdText = "app-init -file com/site0/walnut/ext/wup/hdl/wup_" + mode + ".init ~";
        sys.exec(cmdText);
    }

}
