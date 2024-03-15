package com.site0.walnut.ext.data.o.hdl;

import java.util.List;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnPager;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class o_query extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(pager|hidden|append|mine|quiet|path)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        WnQuery q = new WnQuery();

        // 设置了条件
        if (params.vals.length > 0) {
            for (String val : params.vals) {
                NutMap map = Wlang.map(val);
                q.add(map);
            }
        }
        // 试图从标准输入读取
        else {
            String json = sys.in.readAll();
            if (!Ws.isBlank(json)) {
                Wn.Q.setQuery(q, json);
            }
        }

        // 设置父 ID
        WnObj oP = null;
        String pph = params.getString("p");
        if (!Ws.isBlank(pph)) {
            // 没有父就无视
            if (params.is("quiet")) {
                oP = Wn.getObj(sys, pph);
                if (null == oP) {
                    return;
                }
            }
            // 强制检查
            else {
                oP = Wn.checkObj(sys, pph);
            }
        }
        // 尝试上下文第一个对象
        else if (!fc.list.isEmpty()) {
            oP = fc.list.get(0);
        }
        if (null != oP) {
            NutMap map = Wlang.map("pid", oP.id());
            q.setAllToList(map);
        }

        // 确保是 mine
        if (params.is("mine")) {
            q.setv("d0", "home").setv("d1", sys.getMyGroup());
        }

        WnPager wp = new WnPager(params);

        // 是否要带翻页呢？
        if (wp.countPage) {
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

        // 确保每个对象有路径属性
        if (params.is("path")) {
            for (WnObj o : list) {
                o.path();
            }
        }

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
