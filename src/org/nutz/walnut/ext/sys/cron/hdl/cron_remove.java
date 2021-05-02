package org.nutz.walnut.ext.sys.cron.hdl;

import java.util.List;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sys.cron.WnSysCronApi;
import org.nutz.walnut.ext.sys.cron.WnSysCronQuery;
import org.nutz.walnut.ext.sys.cron.cmd_cron;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value="cqn", regex="^(json)$")
public class cron_remove implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析查询参数
        WnSysCronQuery q = cmd_cron.prepareCronQuery(sys, hc);

        // 准备服务类
        WnSysCronApi cronApi = sys.services.getCronApi();

        // 执行查询
        List<WnObj> list = cronApi.listCronObj(q, false);

        // 逐个删除
        for (WnObj oCron : list) {
            cronApi.removeCronObj(oCron);
        }

        // 输出结果
        cmd_cron.outputCrons(sys, hc, list);
    }

}
