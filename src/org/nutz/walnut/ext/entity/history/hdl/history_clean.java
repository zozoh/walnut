package org.nutz.walnut.ext.entity.history.hdl;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.entity.history.HisQuery;
import org.nutz.walnut.ext.entity.history.HistoryApi;
import org.nutz.walnut.ext.entity.history.WnHistoryService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

public class history_clean implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备接口
        HistoryApi api = hc.getAs("api", WnHistoryService.class);

        // 获取查询条件
        String json = Cmds.getParamOrPipe(sys, hc.params, 0);
        NutMap map = Lang.map(json);
        HisQuery q = Lang.map2Object(map, HisQuery.class);

        // 查询
        int n = api.removeBy(q);

        // 输出结果
        sys.out.println(n);
    }

}
