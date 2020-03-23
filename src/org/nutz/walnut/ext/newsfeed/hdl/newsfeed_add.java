package org.nutz.walnut.ext.newsfeed.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.ext.newsfeed.Newsfeed;
import org.nutz.walnut.ext.newsfeed.WnNewsfeedApi;
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
        WnNewsfeedApi api = hc.getAs("api", WnNewsfeedApi.class);

        // 准备新消息
        String json = Cmds.checkParamOrPipe(sys, hc.params, 0);
        Newsfeed feed = Json.fromJson(Newsfeed.class, json);

        // 准备建表语句
        feed = api.add(feed);

        // 输出
        json = Json.toJson(feed, hc.jfmt);
        sys.out.println(json);
    }

}
