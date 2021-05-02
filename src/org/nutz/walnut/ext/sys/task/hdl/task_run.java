package org.nutz.walnut.ext.sys.task.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.util.ByteInputStream;
import org.nutz.lang.util.Callback;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sys.task.WnSysTask;
import org.nutz.walnut.ext.sys.task.WnSysTaskQuery;
import org.nutz.walnut.ext.sys.task.WnSysTaskService;
import org.nutz.walnut.ext.sys.task.cmd_task;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cqn")
public class task_run implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析查询参数
        WnAccount me = sys.getMe();
        boolean isAdmin = sys.auth.isMemberOfGroup(me, "root");
        WnSysTaskQuery q = cmd_task.prepareTaskQuery(sys, hc, isAdmin);

        // 准备服务类
        WnSysTaskService taskApi = sys.services.getTaskApi();

        // 准备返回列表
        List<WnObj> list = new LinkedList<>();

        // 逐个查询并执行
        while (true) {
            WnSysTask task = taskApi.popTask(q);
            // 木有更多任务了
            if (null == task) {
                break;
            }

            // 看看这个任务是不是我的
            boolean isMyTask = task.meta.creator().equals(me.getName());

            // 不是管理员的话，这任务如果不是自己的，就奇怪了
            if (!isAdmin && !isMyTask) {
                throw Er.create("e.cmd.task.run.NoMine", task.meta.toString());
            }

            // 记录执行的任务
            list.add(task.meta);

            // 准备命令
            String cmdText = task.meta.getString("command");

            // 准备任务的标准输入
            ByteInputStream ins = new ByteInputStream(task.input);

            // 采用自己的账号执行
            if (isMyTask) {
                sys.exec(cmdText, null, null, ins);
            }
            // 切换到目标账号执行
            else {
                WnAccount user = sys.auth.checkAccount(task.meta.creator());
                sys.switchUser(user, new Callback<WnSystem>() {
                    public void invoke(WnSystem sys2) {
                        sys2.exec(cmdText, null, null, ins);
                    }
                });
            }

            // 每次执行后，休息1ms，然后释放一下 CPU
            Lang.quiteSleep(1);
        }

        // 输出结果
        cmd_task.outputTasks(sys, hc, list);
    }

}
