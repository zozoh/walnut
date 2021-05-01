package org.nutz.walnut.ext.sys.task;

import java.util.List;

import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

public class cmd_task extends JvmHdlExecutor {

    /**
     * 根据当前会话设置，准备查询对象
     * 
     * @param sys
     *            系统运行时
     * @param hc
     *            上下文
     * @return 任务对象查询对象
     */
    public static WnSysTaskQuery prepareTaskQuery(WnSystem sys, JvmHdlContext hc) {
        WnSysTaskQuery tq = new WnSysTaskQuery();
        tq.loadFromParams(hc.params);

        // 如果不是 root 组管理员，仅能操作自己
        WnAccount me = sys.getMe();
        if (!sys.auth.isMemberOfGroup(me, "root")) {
            tq.setUserName(me.getName());
        }
        return tq;
    }

    /**
     * 输出任务列表
     * 
     * @param sys
     *            系统运行时
     * @param hc
     *            上下文
     * @param list
     *            任务对象列表
     */
    public static void outputTasks(WnSystem sys, JvmHdlContext hc, List<WnObj> list) {
        if (hc.params.has("t")) {
            hc.params.setDftString("t", "nm,tp,c,g,len,command");
            Cmds.output_objs_as_table(sys, hc.params, null, list);
        }
        // 输出 Bean
        else {
            Cmds.output_objs(sys, hc.params, null, list, true);
        }
    }
}
