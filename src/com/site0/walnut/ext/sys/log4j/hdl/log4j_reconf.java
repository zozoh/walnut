package com.site0.walnut.ext.sys.log4j.hdl;

import org.apache.log4j.PropertyConfigurator;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.role.WnRoleList;
import com.site0.walnut.login.usr.WnUser;

public class log4j_reconf implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        WnUser me = sys.getMe();
        WnRoleList roles = sys.roles().getRoles(me);
        if (!roles.isMemberOfRole("root")) {
            sys.err.print("just for root members");
            return;
        }
        PropertyConfigurator.configure(sys.in.getInputStream());
    }

}
