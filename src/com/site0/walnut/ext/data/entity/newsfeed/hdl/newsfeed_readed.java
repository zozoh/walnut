package com.site0.walnut.ext.data.entity.newsfeed.hdl;

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
public class newsfeed_readed implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备接口
        NewsfeedApi api = hc.getAs("api", WnNewsfeedService.class);

        // 参数
        boolean readed = hc.params.is("read", true);
        String targetId = hc.params.getString("target");

        // 准备返回值
        NutMap re = Wlang.map("n", 0);

        // 标记所有消息
        if (!Strings.isBlank(targetId)) {
            int n = api.setAllReaded(targetId, readed);
            re.put("n", n);
        }
        // 标记单个消息
        else if (hc.params.vals.length > 0) {
            int n = 0;
            for (String val : hc.params.vals) {
                String[] ids = Strings.splitIgnoreBlank(val);
                for (String id : ids) {
                    api.setReaded(id, readed);
                    n++;
                }
            }
            re.put("n", n);
        }

        // 输出

        String json = Json.toJson(re, hc.jfmt);
        sys.out.println(json);
    }

}
