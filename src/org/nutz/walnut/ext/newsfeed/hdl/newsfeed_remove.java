package org.nutz.walnut.ext.newsfeed.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.newsfeed.WnNewsfeedApi;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cqn")
public class newsfeed_remove implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备接口
        WnNewsfeedApi api = hc.getAs("api", WnNewsfeedApi.class);

        // 获取指定的消息 ID 列表
        int n = api.batchRemove(hc.params.vals);

        // 输出结果
        NutMap re = Lang.map("count", n);
        String json = Json.toJson(re, hc.jfmt);
        sys.out.println(json);
    }

}
