package org.nutz.walnut.ext.entity.newsfeed.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.entity.newsfeed.NewfeedApi;
import org.nutz.walnut.ext.entity.newsfeed.Newsfeed;
import org.nutz.walnut.ext.entity.newsfeed.WnNewsfeedApi;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

@JvmHdlParamArgs("cqn")
public class newsfeed_add implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备接口
        NewfeedApi api = hc.getAs("api", WnNewsfeedApi.class);

        // 准备新消息
        String json = Cmds.checkParamOrPipe(sys, hc.params, 0);
        NutMap map = Lang.map(json);
        Newsfeed feed = Lang.map2Object(map, Newsfeed.class);

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
