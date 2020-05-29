package org.nutz.walnut.ext.entity.newsfeed.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.entity.newsfeed.NewfeedApi;
import org.nutz.walnut.ext.entity.newsfeed.WnNewsfeedApi;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cqn")
public class newsfeed_stared implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备接口
        NewfeedApi api = hc.getAs("api", WnNewsfeedApi.class);

        // 参数
        boolean stared = hc.params.is("star", true);

        // 准备返回值
        NutMap re = Lang.map("n", 0);

        // 标记单个消息
        if (hc.params.vals.length > 0) {
            int n = 0;
            for (String val : hc.params.vals) {
                String[] ids = Strings.splitIgnoreBlank(val);
                for (String id : ids) {
                    api.setStared(id, stared);
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
