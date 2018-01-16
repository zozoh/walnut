package org.nutz.walnut.ext.tfodapi.hdl;

import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class tfodt_init implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        sys.out.print("nop yet");
    }

}
