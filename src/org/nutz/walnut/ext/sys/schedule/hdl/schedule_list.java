package org.nutz.walnut.ext.sys.schedule.hdl;

import java.util.List;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sys.schedule.WnSysScheduleQuery;
import org.nutz.walnut.ext.sys.schedule.WnSysScheduleService;
import org.nutz.walnut.ext.sys.schedule.cmd_schedule;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cqn", regex = "^(content)$")
public class schedule_list implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析查询参数
        boolean loadContent = hc.params.is("content");
        WnSysScheduleQuery q = cmd_schedule.prepareSheduleQuery(sys, hc);

        // 准备服务类
        WnSysScheduleService scheduleApi = sys.services.getScheduleApi();

        // 执行查询
        List<WnObj> list = scheduleApi.listSlotObj(q, loadContent);

        // 输出结果
        cmd_schedule.outputScheduleObjs(sys, hc, list);
    }

}
