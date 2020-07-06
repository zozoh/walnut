package org.nutz.walnut.ext.entity.history.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.ext.entity.history.HistoryApi;
import org.nutz.walnut.ext.entity.history.HistoryRecord;
import org.nutz.walnut.ext.entity.history.WnHistoryService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cqn")
public class history_get implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备接口
        HistoryApi api = hc.getAs("api", WnHistoryService.class);

        // 准备历史记录
        String id = hc.params.val_check(0);
        HistoryRecord his = api.fetch(id);

        // 输出
        String json = Json.toJson(his, hc.jfmt);
        sys.out.println(json);
    }

}
