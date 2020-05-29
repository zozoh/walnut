package org.nutz.walnut.ext.entity.newsfeed.hdl;

import org.nutz.dao.QueryResult;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.entity.newsfeed.FeedQuery;
import org.nutz.walnut.ext.entity.newsfeed.NewfeedApi;
import org.nutz.walnut.ext.entity.newsfeed.WnNewsfeedApi;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

@JvmHdlParamArgs("cqn")
public class newsfeed_query implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备接口
        NewfeedApi api = hc.getAs("api", WnNewsfeedApi.class);

        // 获取查询条件
        String json = Cmds.getParamOrPipe(sys, hc.params, 0);
        NutMap map = Lang.map(json);
        FeedQuery fq = Lang.map2Object(map, FeedQuery.class);

        // 设置排序
        if (hc.params.has("sort")) {
            String str = hc.params.get("sort");
            NutMap sort = Lang.map(str);
            fq.setSorts(sort);
        }

        // 准备分页信息
        int pn = hc.params.getInt("pn", 1);
        int pgsz = hc.params.getInt("pgsz", 20);

        // 查询
        QueryResult qr = api.query(fq, pn, pgsz);

        // 输出结果
        json = Json.toJson(qr, hc.jfmt);
        sys.out.println(json);
    }

}
