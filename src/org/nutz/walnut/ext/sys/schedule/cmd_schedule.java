package org.nutz.walnut.ext.sys.schedule;

import java.util.List;

import org.nutz.lang.Times;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sys.task.cmd_task;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.time.WnDayTime;

public class cmd_schedule extends JvmHdlExecutor {

    /**
     * 根据一个字符串输入，自动判断一个时间槽的下标
     * 
     * @param input
     *            一个输入字符串，可支持下面的格式：
     *            <ul>
     *            <li><code>0-1399</code> : 时间槽下标
     *            <li><code>14:00</code> : 直接指定一个时间对应的分钟槽
     *            <li><code>now+1h</code> : 从现在开始 1 小时以后的那个分钟槽
     *            <li><code>now+1m</code> : 从现在开始 1 分钟以后的那个分钟槽
     *            <li><code>now+1s</code> : 从现在开始 1 秒钟以后的那个分钟槽
     *            </ul>
     * @param params
     *            一天中时间槽数量，默认为<code>1440</code>
     * @return 时间槽下标
     */
    public static int timeSlotIndex(String input, int slotN) {
        // 什么都不写，表示第一个时间槽
        if (Ws.isBlank(input)) {
            return 0;
        }
        // 仅仅是一个时间槽下标
        if (input.matches("^(\\d+)$")) {
            return Integer.parseInt(input);
        }
        // 看起来是一个绝对时间
        else if (input.indexOf(':') > 0) {
            WnDayTime time = new WnDayTime(input);
            return timeSlotIndexBySec(time, slotN);
        }
        // 看起来是一个相对时间
        else if (input.startsWith("now")) {
            long ams = Wn.evalDatetimeStrToAMS(input);
            WnDayTime time = new WnDayTime(ams);
            return cmd_schedule.timeSlotIndexBySec(time, slotN);
        }
        // 不认识! -_-
        throw Er.create("e.cmd.schedule.invalidTimeSlot", input);
    }

    /**
     * 根据一天的绝对秒数，取得一个时间槽的下标
     * 
     * @param time
     *            一天中的时间对象
     * @param params
     *            一天中时间槽数量，默认为<code>1440</code>
     * @return 时间槽下标
     */
    public static int timeSlotIndexBySec(WnDayTime time, int slotN) {
        double sec = time.getValue();
        if (slotN <= 0) {
            throw Er.create("e.cmd.schedule.eval_slots.invalidSlotNumber", slotN);
        }
        double unit = (double) 86400 / (double) slotN;
        double dI = sec / unit;
        return (int) dI;
    }

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
        if (!hc.params.is("json")) {
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
