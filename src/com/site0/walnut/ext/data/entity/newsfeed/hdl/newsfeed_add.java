package com.site0.walnut.ext.data.entity.newsfeed.hdl;

import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.data.entity.newsfeed.Newsfeed;
import com.site0.walnut.ext.data.entity.newsfeed.NewsfeedApi;
import com.site0.walnut.ext.data.entity.newsfeed.WnNewsfeedService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;

@JvmHdlParamArgs("cqn")
public class newsfeed_add implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备接口
        NewsfeedApi api = hc.getAs("api", WnNewsfeedService.class);

        // 准备新消息
        String json = Cmds.checkParamOrPipe(sys, hc.params, 0);
        NutMap map = Wlang.map(json);
        Newsfeed feed = Wlang.map2Object(map, Newsfeed.class);

        // 批量插入
        String target = hc.params.get("target");
        String[] taIds = Strings.splitIgnoreBlank(target);

        // 准备返回值
        Object re;

        // 插入一个
        if (null == taIds || taIds.length == 0) {
            re = api.add(feed);
        }
        // 插入多个
        else {
            re = api.batchAdd(feed, taIds);
        }

        // 输出
        json = Json.toJson(re, hc.jfmt);
        sys.out.println(json);
    }

}
