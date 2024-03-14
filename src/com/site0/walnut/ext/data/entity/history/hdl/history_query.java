package com.site0.walnut.ext.data.entity.history.hdl;

import org.nutz.dao.QueryResult;
import org.nutz.dao.pager.Pager;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.data.entity.history.HisQuery;
import com.site0.walnut.ext.data.entity.history.HistoryApi;
import com.site0.walnut.ext.data.entity.history.WnHistoryService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.WnPager;

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
