package org.nutz.walnut.ext.log4j.hdl;

import org.apache.log4j.PropertyConfigurator;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class log4j_reconf implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        WnAccount me = sys.getMe();
        if (!sys.auth.isMemberOfGroup(me, "root")) {
            sys.err.print("just for root members");
            return;
        }
        PropertyConfigurator.configure(sys.in.getInputStream());
    }

}
