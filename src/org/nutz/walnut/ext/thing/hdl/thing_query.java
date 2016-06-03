package org.nutz.walnut.ext.thing.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.thing.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.WnPager;

@JvmHdlParamArgs(value = "cnqihbslVNHQ", regex = "^(pager)$")
public class thing_query implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 找到集合
        WnObj oTS = Things.checkThingSet(hc.oHome);

        // ..............................................
        // 准备分页信息
        WnPager wp = new WnPager(hc.params);

        // ..............................................
        // 准备查询条件
        String qStr = hc.params.val(0);
        WnQuery q = new WnQuery();
        if (!Strings.isBlank(qStr)) {
            // 条件是"或"
            if (Strings.isQuoteBy(qStr, '[', ']')) {
                List<NutMap> ors = Json.fromJsonAsList(NutMap.class, qStr);
                q.addAll(ors);
            }
            // 条件是"与"
            else {
                q.add(Lang.map(qStr));
            }
        }
        // 确保限定了集合
        q.setAllToList(Lang.mapf("th_set:'%s',th_live:%d", oTS.id(), Things.TH_LIVE));

        // ..............................................
        // 设置分页信息
        if (null != wp) {
            wp.setupQuery(sys, q);
        }

        // 设置排序
        if (hc.params.has("sort")) {
            NutMap sort = Lang.map(hc.params.check("sort"));
            q.sort(sort);
        }

        // ..............................................
        // 执行查询并返回结果
        List<WnObj> list = sys.io.query(q);

        hc.pager = wp;
        hc.output = list;
    }

}
