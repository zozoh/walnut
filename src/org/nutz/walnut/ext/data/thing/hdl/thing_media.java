package org.nutz.walnut.ext.data.thing.hdl;

import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cnqVNHQ", regex = "^(del|quiet|overwrite|ufc)$")
public class thing_media implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        hc.output = hc.doOtherHandler("file", (hc2) -> {
            hc2.params.setv("dir", "media");
        });
    }

}