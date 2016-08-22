package org.nutz.walnut.ext.hmaker.hdl;

import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class hmaker_init implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        sys.execf("app-init -file %s/hmaker_%s.init ~",
                  hmaker_init.class.getPackage().getName().replace('.', '/'),
                  hc.params.get("mode", "0"));

    }

}
