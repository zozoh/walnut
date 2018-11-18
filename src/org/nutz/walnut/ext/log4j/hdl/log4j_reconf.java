package org.nutz.walnut.ext.log4j.hdl;

import org.apache.log4j.PropertyConfigurator;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class log4j_reconf implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        int roleInRoot = sys.usrService.getRoleInGroup(sys.me, "root");
        boolean I_am_member_of_root = roleInRoot == 1 || roleInRoot == 10;
        if (!I_am_member_of_root) {
            sys.err.print("just for root members");
            return;
        }
        PropertyConfigurator.configure(sys.in.getInputStream());
    }

}
