package org.nutz.walnut.ext.sys.schedule.hdl;

import java.util.List;

import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sys.schedule.WnSysScheduleApi;
import org.nutz.walnut.ext.sys.schedule.WnSysScheduleQuery;
import org.nutz.walnut.ext.sys.schedule.cmd_schedule;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value="cqn", regex="^(json)$")
public class schedule_clean implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 只有系统管理员才能执行
        WnAccount me = sys.getMe();
        if (!sys.auth.isMemberOfGroup(me, "root")) {
            throw Er.create("e.cmd.schedule.load", "You must be admin!");
        }

        // 分析查询参数
        WnSysScheduleQuery q = cmd_schedule.prepareSheduleQuery(sys, hc);

        // 准备服务类
        WnSysScheduleApi scheduleApi = sys.services.getScheduleApi();

        // 执行查询
        List<WnObj> list = scheduleApi.cleanSlotObj(q);

        // 输出结果
        cmd_schedule.outputScheduleObjs(sys, hc, list);
    }

}