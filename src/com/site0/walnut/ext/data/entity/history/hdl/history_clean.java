package com.site0.walnut.ext.data.entity.history.hdl;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.data.entity.history.HisQuery;
import com.site0.walnut.ext.data.entity.history.HistoryApi;
import com.site0.walnut.ext.data.entity.history.WnHistoryService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;

@JvmHdlParamArgs("cqn")
public class history_clean implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备接口
        HistoryApi api = hc.getAs("api", WnHistoryService.class);

        // 获取查询条件
        String json = Cmds.getParamOrPipe(sys, hc.params, 0);
        NutMap map = Wlang.map(json);
        if(map.isEmpty() && !hc.params.is("all")) {
            throw Er.create("e.cmd.history.implicitCleanAll");
        }

        HisQuery q = Wlang.map2Object(map, HisQuery.class);


        // 查询
        int n = api.removeBy(q);

        // 输出结果
        sys.out.println(n);
    }

}
