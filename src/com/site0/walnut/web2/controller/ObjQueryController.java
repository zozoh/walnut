package com.site0.walnut.web2.controller;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.mvc.AbstractWnController;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcRequest;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.WnPagerObj;

/**
 * <code>/o/query</code>：对照旧 <code>ObjModule.query(228)</code>（RM-NUTZ-08 审核表 #5）。
 * <p>
 * 旧 <code>@Param("..") WnQuery q</code> 走 Nutz <code>ObjectPairInjector</code>
 * （POJO 按字段绑定）：只有 <code>limit</code>/<code>skip</code>/<code>sort</code>/
 * <code>list</code>/<code>parentObj</code> 五个参数会注入，其余请求参数一律忽略
 * （2026-11-10 用户定稿，见任务文档决策记录）。其中 <code>sort</code> 收 JSON 串转
 * NutMap、<code>list</code> 收 JSON 数组串转查询条件；<code>parentObj</code> 无有效
 * 字符串转换，忽略。分页方法体逐行照抄。
 */
public class ObjQueryController extends AbstractWnController {

    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        WnMvcRequest req = ctx.request();
        try {
            // 2026-11-10 用户裁定：GET-only，参数只从 query string 取（不读 body form）
            NutMap pm = req.queryStringMap();
            WnQuery q = new WnQuery();
            q.limit(pm.getInt("limit", 0));
            q.skip(pm.getInt("skip", 0));
            String sort = pm.getString("sort");
            if (null != sort) {
                q.sort(Json.fromJson(NutMap.class, sort));
            }
            String listJson = pm.getString("list");
            if (null != listJson) {
                List<?> maps = Json.fromJson(List.class, listJson);
                for (Object ele : maps) {
                    q.add((NutMap) ele);
                }
            }

            boolean isPaging = q.limit() > 0;

            // 确保数据的限制
            if (q.limit() > 5000) {
                q.limit(5000);
            } else if (q.limit() <= 0) {
                q.limit(100);
            }

            // 查询列表
            List<WnObj> list = io(ctx).query(q);

            // 需要分页
            if (isPaging) {
                WnPagerObj pager = new WnPagerObj().setBy(q);
                long tc = io(ctx).count(q);
                pager.setTotal(tc);
                return WnMvcView.ajax(new NutMap("list", list).setv("pager", pager));
            }

            // 直接返回列表
            return WnMvcView.ajax(list);
        }
        catch (RuntimeException e) {
            return businessFailure(e);
        }
    }

}
