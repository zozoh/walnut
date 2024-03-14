package com.site0.walnut.ext.data.thing.hdl;

import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cnqVNHQ", regex = "^(del|quiet|overwrite|ufc)$")
public class thing_attachment implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        hc.output = hc.doOtherHandler("file", (hc2) -> {
            hc2.params.setv("dir", "attachment");
        });
    }

}
