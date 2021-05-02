package org.nutz.walnut.ext.sys.schedule.hdl;

import java.util.List;

import org.nutz.walnut.ext.sys.cron.WnSysCron;
import org.nutz.walnut.ext.sys.cron.WnSysCronQuery;
import org.nutz.walnut.ext.sys.cron.WnSysCronService;
import org.nutz.walnut.ext.sys.cron.cmd_cron;
import org.nutz.walnut.ext.sys.schedule.WnCronSlot;
import org.nutz.walnut.ext.sys.schedule.WnSysScheduleQuery;
import org.nutz.walnut.ext.sys.schedule.WnSysScheduleService;
import org.nutz.walnut.ext.sys.schedule.cmd_schedule;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cqn", regex = "^(force)$")
public class schedule_load implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析查询参数
        boolean force = hc.params.is("force");
        WnSysScheduleQuery q0 = cmd_schedule.prepareSheduleQuery(sys, hc);
        WnSysCronQuery q1 = cmd_cron.prepareCronQuery(sys, hc);

        // 准备服务类
        WnSysCronService cronApi = sys.services.getCronApi();
        WnSysScheduleService scheduleApi = sys.services.getScheduleApi();

        // 加载定期任务对象列表
        List<WnSysCron> crons = cronApi.listCron(q1, true);

        // 如果强制加载的话，先清除
        if (force) {
            scheduleApi.cleanSlotObj(q0);
        }

        // 加载到分钟计划表
        List<WnCronSlot> slots = scheduleApi.loadSchedule(crons, q0.getToday(), force);

        // 输出
        cmd_schedule.outputSchedulesSlots(sys, hc, slots);
    }

}
