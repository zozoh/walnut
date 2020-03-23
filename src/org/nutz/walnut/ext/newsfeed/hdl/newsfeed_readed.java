package org.nutz.walnut.ext.newsfeed.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.newsfeed.WnNewsfeedApi;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cqn", regex = "^(read)$")
public class newsfeed_readed implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备接口
        WnNewsfeedApi api = hc.getAs("api", WnNewsfeedApi.class);

        // 参数
        boolean read = hc.params.is("read", true);
        String targetId = hc.params.get("target");

        // 准备返回值
        NutMap re = Lang.map("n", 0);

        // 标记所有消息
        if (!Strings.isBlank(targetId)) {
            int n = api.setAllRead(targetId, read);
            re.put("n", n);
        }
        // 标记单个消息
        else if (hc.params.vals.length > 0) {
            for (String id : hc.params.vals) {
                api.setRead(id, read);
            }
            re.put("n", hc.params.vals.length);
        }

        // 输出
        String json = Json.toJson(re, hc.jfmt);
        sys.out.println(json);
    }

}
