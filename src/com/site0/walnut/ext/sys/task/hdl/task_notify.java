package com.site0.walnut.ext.sys.task.hdl;

import com.site0.walnut.ext.sys.task.WnSysTaskApi;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class task_notify implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        WnSysTaskApi taskApi = sys.services.getTaskApi();
        taskApi.notifyForNewTaskComing();
    }

}