package org.nutz.walnut.ext.o.hdl;

import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.o.OContext;
import org.nutz.walnut.ext.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.WnPager;
import org.nutz.walnut.util.ZParams;

public class o_query extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(pager|append|mine)$");
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
        // 无论如何也得添加一个
        else {
            WnObj oCu = sys.getCurrentObj();
            NutMap map = Lang.map("pid", oCu.id());
            q.add(map);
        }

        // 设置父 ID
        if (!fc.list.isEmpty()) {
            WnObj o = fc.list.get(0);
            NutMap map = Lang.map("pid", o.id());
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

        if (!params.is("append")) {
            fc.list.clear();
        }
        fc.list.addAll(list);
    }

}
