package org.nutz.walnut.ext.o.hdl;

import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.o.OContext;
import org.nutz.walnut.ext.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnPager;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class o_query extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(pager|hidden|append|mine)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        WnQuery q = new WnQuery();

        if (params.vals.length > 0) {
            for (String val : params.vals) {
                NutMap map = Lang.map(val);
                q.add(map);
            }
        }

        // 设置父 ID
        WnObj oP = null;
        String pph = params.getString("p");
        if (!Ws.isBlank(pph)) {
            oP = Wn.checkObj(sys, pph);
        }
        // 尝试上下文第一个对象
        else if (!fc.list.isEmpty()) {
            oP = fc.list.get(0);
        }
        if (null != oP) {
            NutMap map = Lang.map("pid", oP.id());
            q.setAllToList(map);
        }

        // 确保是 mine
        if (params.is("mine")) {
            q.setv("d0", "home").setv("d1", sys.getMyGroup());
        }

        WnPager wp = new WnPager(params);

        // 是否要带翻页呢？
        if (params.is("pager")) {
            fc.pager = wp;
        }

        // 设置分页
        wp.setupQuery(sys, q);

        // 设置排序
        NutMap sort = params.getMap("sort");
        if (null != sort && !sort.isEmpty()) {
            q.sort(sort);
        }

        // 执行查询结果
        List<WnObj> list = sys.io.query(q);

        // 清除上下文
        if (!params.is("append")) {
            fc.list.clear();
        }

        // 全部加入上下文
        if (params.is("hidden")) {
            fc.list.addAll(list);
        }
        // 删除隐藏对象
        else {
            for (WnObj o : list) {
                if (!o.isHidden()) {
                    fc.list.add(o);
                }
            }
        }

    }

}
