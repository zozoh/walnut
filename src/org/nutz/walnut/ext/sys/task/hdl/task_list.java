package org.nutz.walnut.ext.sys.task.hdl;

import java.util.List;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sys.task.WnSysTaskQuery;
import org.nutz.walnut.ext.sys.task.WnSysTaskService;
import org.nutz.walnut.ext.sys.task.cmd_task;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("cqn")
public class task_list implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析查询参数
        WnSysTaskQuery q = cmd_task.prepareTaskQuery(sys, hc);

        // 准备服务类
        WnSysTaskService taskApi = Wn.Service.tasks();

        // 执行查询
        List<WnObj> list = taskApi.listTasks(q);

        // 输出结果
        cmd_task.outputTasks(sys, hc, list);
    }

}
