package com.site0.walnut.ext.sys.schedule.hdl;

import java.util.List;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.sys.schedule.WnCronSlot;
import com.site0.walnut.ext.sys.schedule.WnSysScheduleApi;
import com.site0.walnut.ext.sys.schedule.WnSysScheduleQuery;
import com.site0.walnut.ext.sys.schedule.cmd_schedule;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.role.WnRoleList;
import com.site0.walnut.login.usr.WnUser;

@JvmHdlParamArgs(value = "cqn", regex = "^(keep)$")
public class schedule_push implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 只有系统管理员才能执行
        WnUser me = sys.getMe();
        WnRoleList roles = sys.auth.getRoles(me);
        if (!roles.isMemberOfRole("root")) {
            throw Er.create("e.cmd.schedule.load", "You must be admin!");
        }

        // 分析查询参数
        boolean keep = hc.params.is("keep");
        WnSysScheduleQuery q = cmd_schedule.prepareSheduleQuery(sys, hc);

        // 准备服务类
        WnSysScheduleApi scheduleApi = sys.services.getScheduleApi();

        // 执行查询
        List<WnCronSlot> slots = scheduleApi.listSlot(q, true);

        // 推入任务堆栈
        scheduleApi.pushSchedule(slots, keep);

        // 输出结果
        cmd_schedule.outputSchedulesSlots(sys, hc, slots);
    }

}
