package com.site0.walnut.ext.sys.task.hdl;

import java.util.List;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.task.WnSysTaskApi;
import com.site0.walnut.ext.sys.task.WnSysTaskQuery;
import com.site0.walnut.ext.sys.task.cmd_task;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cqn", regex = "^(json)$")
public class task_list implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析查询参数
        WnSysTaskQuery q = cmd_task.prepareTaskQuery(sys, hc);

        // 准备服务类
        WnSysTaskApi taskApi = sys.services.getTaskApi();

        // 执行查询
        List<WnObj> list = taskApi.listTasks(q);

        // 输出结果
        cmd_task.outputTasks(sys, hc, list);
    }

}
