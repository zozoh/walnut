package org.nutz.walnut.ext.wooz.hdl;

import org.nutz.lang.Lang;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value="cqn", regex="^(write)$")
public class wooz_seq implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        throw Lang.noImplement();
    }

}
