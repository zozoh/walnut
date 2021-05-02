package org.nutz.walnut.ext.sys.schedule;

import java.util.List;

import org.nutz.lang.Times;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sys.task.cmd_task;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

public class cmd_schedule extends JvmHdlExecutor {

    /**
     * 根据当前会话设置，准备查询对象
     * 
     * @param sys
     *            系统运行时
     * @param hc
     *            上下文
     * @return 任务对象查询对象
     */
    public static WnSysScheduleQuery prepareSheduleQuery(WnSystem sys, JvmHdlContext hc) {
        WnSysScheduleQuery q = new WnSysScheduleQuery();
        q.loadFromParams(hc.params);

        // 如果不是 root 组管理员，仅能操作自己
        WnAccount me = sys.getMe();
        if (!sys.auth.isMemberOfGroup(me, "root")) {
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
    public static void outputScheduleObjs(WnSystem sys, JvmHdlContext hc, List<WnObj> list) {
        if (hc.params.has("t")) {
            hc.params.setDftString("t", "time,nm,user,task,cron,content");
            hc.params.setv("b", true);
            hc.params.setv("i", true);
            hc.params.setv("s", true);
            hc.params.setv("h", true);
            cmd_task.formatObjCommandField(list, "content");

            // 为每个时间槽对象声明上可阅读的时间值
            for (WnObj o : list) {
                int slot = o.getInt("slot");
                Times.TmInfo ti = Times.Ti(slot * 60);
                String ts = ti.toString();
                o.put("time", ts);
            }

            Cmds.output_objs_as_table(sys, hc.params, null, list);
        }
        // 输出 Bean
        else {
            hc.params.setv("l", true);
            Cmds.output_objs(sys, hc.params, null, list, true);
        }
    }

    public static void outputSchedulesSlots(WnSystem sys, JvmHdlContext hc, List<WnCronSlot> list) {
        int i = 0;
        for (WnCronSlot slot : list) {
            sys.out.printlnf("%02d) %s", i++, slot.toString());
        }
        sys.out.println("-----------------------------------");
        sys.out.printlnf("Total %d items", list.size());
    }

}
