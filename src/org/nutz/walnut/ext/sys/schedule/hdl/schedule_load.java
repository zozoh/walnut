package org.nutz.walnut.ext.sys.schedule.hdl;

import java.util.Date;
import java.util.List;

import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.sys.cron.WnSysCron;
import org.nutz.walnut.ext.sys.cron.WnSysCronApi;
import org.nutz.walnut.ext.sys.cron.WnSysCronQuery;
import org.nutz.walnut.ext.sys.cron.cmd_cron;
import org.nutz.walnut.ext.sys.schedule.WnSysScheduleApi;
import org.nutz.walnut.ext.sys.schedule.bean.WnMinuteSlotIndex;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.Wtime;

@JvmHdlParamArgs(value = "cqn", regex = "^(force|json)$")
public class schedule_load implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 只有系统管理员才能执行
        WnAccount me = sys.getMe();
        if (!sys.auth.isMemberOfGroup(me, "root")) {
            throw Er.create("e.cmd.schedule.load", "You must be admin!");
        }

        // 分析查询参数
        boolean force = hc.params.is("force");
        WnSysCronQuery q = cmd_cron.prepareCronQuery(sys, hc);

        // 得到指定日期
        String today = hc.params.getString("today", "today");
        long ams = Wtime.valueOf(today);
        Date d = new Date(ams);

        // 从哪个分钟槽开始
        String slot = hc.params.getString("slot", "now");
        int amount = hc.params.getInt("amount", 3);

        // 准备服务类
        WnSysCronApi cronApi = sys.services.getCronApi();
        WnSysScheduleApi scheduleApi = sys.services.getScheduleApi();

        // 加载定期任务对象列表
        List<WnSysCron> crons = cronApi.listCron(q, true);

        // 加载到分钟计划表
        List<WnMinuteSlotIndex> list = scheduleApi.loadSchedule(crons, d, slot, amount, force);

        // 输出
        String str = "No Cron Tasks";
        if (!list.isEmpty()) {
            str = Ws.join(list, "---------\n");
        }
        sys.out.println(str);
    }

}
