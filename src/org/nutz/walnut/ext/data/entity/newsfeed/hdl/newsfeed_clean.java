package org.nutz.walnut.ext.data.entity.newsfeed.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.data.entity.newsfeed.NewsfeedApi;
import org.nutz.walnut.ext.data.entity.newsfeed.WnNewsfeedService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class newsfeed_clean implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备接口
        NewsfeedApi api = hc.getAs("api", WnNewsfeedService.class);

        // 参数
        String targetId = hc.params.get("target");

        // 准备返回值
        NutMap re = Lang.map("n", 0);

        // 清除所有指定 target 的消息
        if (!Strings.isBlank(targetId)) {
            int n = api.cleanAllReaded(targetId);
            re.put("n", n);
        }

        // 输出
        String json = Json.toJson(re, hc.jfmt);
        sys.out.println(json);
    }

}
