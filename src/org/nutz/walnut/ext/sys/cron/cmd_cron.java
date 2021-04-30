package org.nutz.walnut.ext.sys.cron;

import java.util.List;

import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

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
        WnSysCronQuery tq = new WnSysCronQuery();
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
    public static void outputCrons(WnSystem sys, JvmHdlContext hc, List<WnObj> list) {
        if (hc.params.has("t")) {
            hc.params.setDftString("t", "id,tp,user,cron,content");
            Cmds.output_objs_as_table(sys, hc.params, null, list);
        }
        // 输出 Bean
        else {
            Cmds.output_objs(sys, hc.params, null, list, true);
        }
    }

}
