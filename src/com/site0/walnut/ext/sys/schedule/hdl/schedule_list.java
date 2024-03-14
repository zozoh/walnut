package com.site0.walnut.ext.sys.schedule.hdl;

import java.util.List;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.schedule.WnSysScheduleApi;
import com.site0.walnut.ext.sys.schedule.WnSysScheduleQuery;
import com.site0.walnut.ext.sys.schedule.cmd_schedule;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cqn", regex = "^(content|json)$")
public class schedule_list implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析查询参数
        boolean loadContent = hc.params.is("content");
        WnSysScheduleQuery q = cmd_schedule.prepareSheduleQuery(sys, hc);

        // 准备服务类
        WnSysScheduleApi scheduleApi = sys.services.getScheduleApi();

        // 执行查询
        List<WnObj> list = scheduleApi.listSlotObj(q, loadContent);

        // 输出结果
        cmd_schedule.outputScheduleObjs(sys, hc, list);
    }

}
