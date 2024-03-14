package com.site0.walnut.ext.data.entity.history.hdl;

import org.nutz.json.Json;
import com.site0.walnut.ext.data.entity.history.HistoryApi;
import com.site0.walnut.ext.data.entity.history.HistoryRecord;
import com.site0.walnut.ext.data.entity.history.WnHistoryService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

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
