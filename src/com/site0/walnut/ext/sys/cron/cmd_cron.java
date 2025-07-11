package com.site0.walnut.ext.sys.cron;

import java.util.List;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.task.cmd_task;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.role.WnRoleList;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Cmds;

public class cmd_cron extends JvmHdlExecutor {

    /**
     * 根据当前会话设置，准备查询对象
     * 
     * @param sys
     *            系统运行时
     * @param hc
     *            上下文
     * @return 任务对象查询对象
     */
    public static WnSysCronQuery prepareCronQuery(WnSystem sys, JvmHdlContext hc) {
        WnSysCronQuery q = new WnSysCronQuery();
        q.loadFromParams(hc.params);

        // 如果不是 root 组管理员，仅能操作自己
        WnUser me = sys.getMe();
        WnRoleList roles = sys.auth.getRoles(me);
        if (!roles.isMemberOfRole("root")) {
            q.setUserName(me.getName());
        }
        return q;
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
    public static void outputCrons(WnSystem sys, JvmHdlContext hc, List<WnObj> list) {
        if (!hc.params.is("json")) {
            hc.params.setDftString("t", "id,user,cron,content");
            hc.params.setv("b", true);
            hc.params.setv("i", true);
            hc.params.setv("s", true);
            hc.params.setv("h", true);
            cmd_task.formatObjCommandField(list, "content");
            Cmds.output_objs_as_table(sys, hc.params, null, list);
        }
        // 输出 Bean
        else {
            hc.params.setv("l", true);
            Cmds.output_objs(sys, hc.params, null, list, true);
        }
    }

}
