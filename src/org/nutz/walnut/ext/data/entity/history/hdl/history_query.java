package org.nutz.walnut.ext.data.entity.history.hdl;

import org.nutz.dao.QueryResult;
import org.nutz.dao.pager.Pager;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.data.entity.history.HisQuery;
import org.nutz.walnut.ext.data.entity.history.HistoryApi;
import org.nutz.walnut.ext.data.entity.history.WnHistoryService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.WnPager;

@JvmHdlParamArgs("cqn")
public class history_query implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备接口
        HistoryApi api = hc.getAs("api", WnHistoryService.class);

        // 获取查询条件
        String json = Cmds.getParamOrPipe(sys, hc.params, 0);
        NutMap map = Lang.map(json);
        HisQuery q = Lang.map2Object(map, HisQuery.class);

        // 设置排序
        if (hc.params.has("sort")) {
            String str = hc.params.get("sort");
            NutMap sort = Lang.map(str);
            q.setSorts(sort);
        }

        // 准备分页信息
        int pn = hc.params.getInt("pn", 1);
        int pgsz = hc.params.getInt("pgsz", 20);

        // 查询
        QueryResult qr = api.query(q, pn, pgsz);

        // 转换结果
        Pager pg = qr.getPager();
        WnPager wp = new WnPager(pg);
        NutMap re = Cmds.createQueryResult(wp, qr.getList());

        // 输出结果
        json = Json.toJson(re, hc.jfmt);
        sys.out.println(json);
    }

}
