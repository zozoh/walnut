package com.site0.walnut.web2.controller;

import java.util.List;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.mvc.AbstractWnController;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcRequest;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnPagerObj;

/**
 * <code>/o/children</code>：对照旧 <code>ObjModule.children(254)</code>（RM-NUTZ-08
 * 审核表 #6）。逐行照抄旧方法体（<code>@Param("pg") Boolean paging</code> 缺省为
 * <code>null</code>，<code>!paging</code> 抛 NPE 的旧语义保留——缺 <code>pg</code>
 * 即失败）。过滤器 <code>SessionCheckFilter()</code>。
 */
public class ObjChildrenController extends AbstractWnController {

    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        WnMvcRequest req = ctx.request();
        try {
            // 2026-11-10 用户裁定：GET-only，参数只从 query string 取（不读 body form）
            NutMap pm = req.queryStringMap();
            String str = pm.getString("str");
            // 旧 @Param("pg") Boolean：缺省为 null（NutBean.getBoolean(String) 是
            // primitive 会吃掉 null，必须按 key 存在性判断）
            Boolean paging = pm.containsKey("pg") ? pm.getBoolean("pg") : null;
            WnSession se = Wn.WC().checkSession();
            WnObj o = Wn.checkObj(io(ctx), se, str);

            // 查询
            List<WnObj> list = io(ctx).getChildren(o, null);

            // 不要翻页信息
            if (!paging)
                return WnMvcView.ajax(list);

            // 更新分页信息
            int limit = Math.max(100, list.size());
            WnPagerObj pager = new WnPagerObj().set(limit, 0);
            pager.setTotal(list.size());

            // 返回
            return WnMvcView.ajax(new NutMap("list", list).setv("pager", pager));
        }
        catch (RuntimeException e) {
            return businessFailure(e);
        }
    }

}
