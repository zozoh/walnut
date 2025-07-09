package com.site0.walnut.ext.sys.schedule.hdl;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.WnRoleList;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.util.Wlang;

public class schedule_awake implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 只有系统管理员才能执行
        WnUser me = sys.getMe();
        WnRoleList roles = sys.auth.getRoles(me);
        if (!roles.isMemberOfRole("root")) {
            throw Er.create("e.cmd.schedule.load", "You must be admin!");
        }

        Object lock = sys.services.getScheduleApi();
        Wlang.notifyAll(lock);
    }

}
