package com.site0.walnut.ext.data.entity.newsfeed.hdl;

import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.data.entity.newsfeed.NewsfeedApi;
import com.site0.walnut.ext.data.entity.newsfeed.WnNewsfeedService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class newsfeed_clean implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备接口
        NewsfeedApi api = hc.getAs("api", WnNewsfeedService.class);

        // 参数
        String targetId = hc.params.get("target");

        // 准备返回值
        NutMap re = Wlang.map("n", 0);

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
