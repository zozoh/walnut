package com.site0.walnut.web2.controller;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.mvc.AbstractWnController;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcRequest;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnPagerObj;

/**
 * <code>/o/find</code>：对照旧 <code>ObjModule.find(303)</code>（RM-NUTZ-08 审核表 #7）。
 * <code>@Param("..") NutMap match</code> = 全部请求参数（<b>含 <code>_</code> 开头的
 * 键</b>，方法体内的剔除逻辑逐行照抄）；<code>_me</code> 缺省 <code>true</code>。
 * 过滤器 <code>SessionCheckFilter()</code>。
 */
public class ObjFindController extends AbstractWnController {

    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        WnMvcRequest req = ctx.request();
        try {
            // 2026-11-10 用户裁定：GET-only，match = 全部 query string 参数（不读 body form）
            NutMap pm = req.queryStringMap();
            int limit = pm.getInt("_l", 0);
            int skip = pm.getInt("_o", 0);
            String sort = pm.getString("_s");
            boolean mine = pm.getBoolean("_me", true);
            NutMap match = pm;

            // 准备查询
            WnQuery q = new WnQuery();

            // 翻页信息
            WnPagerObj pager = new WnPagerObj().set(limit, skip);
            pager.setupQuery(q);

            // 排序
            NutMap sorting = Wlang.map(sort);
            q.sort(sorting);

            // 确保去掉 "_" 开头的字段
            List<String> dks = new ArrayList<>(match.size());
            for (String k : match.keySet()) {
                if (k.startsWith("_"))
                    dks.add(k);
            }
            for (String k : dks) {
                match.remove(k);
            }

            // 添加查询条件
            q.setAll(match);

            // 增加自己主域限制
            if (mine) {
                WnSession se = Wn.WC().checkSession();
                q.setv("d0", "home");
                q.setv("d1", se.getMyGroup());
            }

            // 查询
            List<WnObj> list = io(ctx).query(q);

            // 更新分页信息
            long tc = io(ctx).count(q);
            pager.setTotal(tc);
            pager.setCount(list.size());

            // 返回
            return WnMvcView.ajax(new NutMap("list", list).setv("pager", pager));
        }
        catch (RuntimeException e) {
            return businessFailure(e);
        }
    }

}
