package org.nutz.walnut.ext.entity.newsfeed.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.entity.newsfeed.WnNewsfeedApi;
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

        // 准备解析列表
        List<String> list = new LinkedList<>();
        for (String val : hc.params.vals) {
            String[] vs = Strings.splitIgnoreBlank(val);
            for (String v : vs) {
                list.add(v);
            }
        }

        // 获取指定的消息 ID 列表
        int n = 0;
        if (!list.isEmpty()) {
            String[] ids = list.toArray(new String[list.size()]);
            n = api.batchRemove(ids);
        }

        // 输出结果
        NutMap re = Lang.map("count", n);
        String json = Json.toJson(re, hc.jfmt);
        sys.out.println(json);
    }

}
