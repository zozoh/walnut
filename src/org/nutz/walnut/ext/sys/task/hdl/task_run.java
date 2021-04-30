package org.nutz.walnut.ext.sys.task.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.util.ByteInputStream;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sys.task.WnSysTask;
import org.nutz.walnut.ext.sys.task.WnSysTaskQuery;
import org.nutz.walnut.ext.sys.task.WnSysTaskService;
import org.nutz.walnut.ext.sys.task.cmd_task;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

@JvmHdlParamArgs("cqn")
public class task_run implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析查询参数
        WnSysTaskQuery q = cmd_task.prepareTaskQuery(sys, hc);

        // 准备服务类
        WnSysTaskService taskApi = Wn.Service.tasks();

        // 准备返回列表
        List<WnObj> list = new LinkedList<>();

        // 逐个查询并执行
        while (true) {
            WnSysTask task = taskApi.popTask(q);
            // 木有更多任务了
            if (null == task) {
                break;
            }

            // 记录执行的任务
            list.add(task.meta);

            // 准备命令
            String[] commands = task.meta.getArray("commands", String.class);
            String cmdText = Ws.join(commands, "; ");

            // 准备任务的标准输入
            ByteInputStream ins = new ByteInputStream(task.input);

            // 执行
            sys.exec(cmdText, null, null, ins);

            // 每次执行后，休息1ms，然后释放一下 CPU
            Lang.quiteSleep(1);
        }

        // 输出结果
        cmd_task.outputTasks(sys, hc, list);
    }

}
