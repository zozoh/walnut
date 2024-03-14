package com.site0.walnut.ext.sys.task.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.ByteInputStream;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.task.WnSysTask;
import com.site0.walnut.ext.sys.task.WnSysTaskApi;
import com.site0.walnut.ext.sys.task.WnSysTaskQuery;
import com.site0.walnut.ext.sys.task.cmd_task;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;

@JvmHdlParamArgs(value = "cqn", regex = "^(json)$")
public class task_run implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析查询参数
        WnAccount me = sys.getMe();
        boolean I_am_admin = sys.auth.isMemberOfGroup(me, "root");
        WnSysTaskQuery q = cmd_task.prepareTaskQuery(sys, hc, I_am_admin);
        int limit = q.getLimit();

        // 准备服务类
        WnSysTaskApi taskApi = sys.services.getTaskApi();

        // 准备返回列表
        List<WnObj> list = new LinkedList<>();

        // 逐个查询并执行
        while (true) {
            // 最多执行多少条命令
            if (limit > 0 && list.size() >= limit) {
                break;
            }

            WnSysTask task = taskApi.popTask(q);
            // 木有更多任务了
            if (null == task) {
                break;
            }

            // 看看这个任务是不是我的
            String userName = task.meta.getString("user");
            boolean isMyTask = me.isSameName(userName);

            // 不是管理员的话，这任务如果不是自己的，就奇怪了
            if (!I_am_admin && !isMyTask) {
                throw Er.create("e.cmd.task.run.NoMine", task.meta.toString());
            }

            // 检查执行账号
            WnAccount user = null;
            if (!isMyTask) {
                user = sys.auth.checkAccount(userName);
            }

            // 准备任务的标准输入
            ByteInputStream ins = new ByteInputStream(task.input);

            // 执行任务对象
            taskApi.runTask(sys, task.meta, user, ins);

            // 记录执行的任务
            list.add(task.meta);

            // 每次执行后，休息1ms，然后释放一下 CPU
            Wlang.quiteSleep(1);
        }

        // 输出结果
        cmd_task.outputTasks(sys, hc, list);
    }

}
