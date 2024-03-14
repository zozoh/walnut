package com.site0.walnut.ext.data.entity.history.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.data.entity.history.HistoryApi;
import com.site0.walnut.ext.data.entity.history.HistoryRecord;
import com.site0.walnut.ext.data.entity.history.WnHistoryService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;

@JvmHdlParamArgs("cqn")
public class history_add implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备接口
        HistoryApi api = hc.getAs("api", WnHistoryService.class);

        // 准备历史记录
        String json = Cmds.checkParamOrPipe(sys, hc.params, 0);
        NutMap map = Lang.map(json);
        HistoryRecord his = Lang.map2Object(map, HistoryRecord.class);

        // 准备返回值
        Object re = api.add(his);

        // 输出
        json = Json.toJson(re, hc.jfmt);
        sys.out.println(json);
    }

}
