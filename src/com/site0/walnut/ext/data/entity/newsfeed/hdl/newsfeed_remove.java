package com.site0.walnut.ext.data.entity.newsfeed.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.data.entity.newsfeed.NewsfeedApi;
import com.site0.walnut.ext.data.entity.newsfeed.WnNewsfeedService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cqn")
public class newsfeed_remove implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备接口
        NewsfeedApi api = hc.getAs("api", WnNewsfeedService.class);

        // 准备解析列表
        List<String> list = new LinkedList<>();
        for (String val : hc.params.vals) {
            String[] vs = Strings.splitIgnoreBlank(val);
            for (String v : vs) {
                list.add(v);
            }
        }

        // 获取指定的消息 ID 列表
        String[] ids = list.toArray(new String[list.size()]);
        int n = api.remove(ids);

        // 输出结果
        NutMap re = Wlang.map("count", n);
        String json = Json.toJson(re, hc.jfmt);
        sys.out.println(json);
    }

}
