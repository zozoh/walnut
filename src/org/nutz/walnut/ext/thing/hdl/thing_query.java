package org.nutz.walnut.ext.thing.hdl;

import org.nutz.lang.Lang;
import org.nutz.walnut.ext.thing.impl.QueryThingAction;
import org.nutz.walnut.ext.thing.util.ThQr;
import org.nutz.walnut.ext.thing.util.ThQuery;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.WnPager;

@JvmHdlParamArgs(value = "cnqihbslVNHQ", regex = "^(pager|content|obj)$")
public class thing_query implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        ThQuery tq = new ThQuery();

        // ..............................................
        // 准备分页信息
        tq.wp = new WnPager(hc.params);

        // ..............................................
        // 准备查询条件
        tq.qStr = Cmds.getParamOrPipe(sys, hc.params, 0);

        // ..............................................
        // 确保限定了集合
        tq.tss = hc.params.getAs("tss", String[].class);

        // 设置排序
        if (hc.params.hasString("sort")) {
            tq.sort = Lang.map(hc.params.check("sort"));
        }

        tq.needContent = hc.params.is("obj");
        tq.autoObj = hc.params.is("obj");

        QueryThingAction TA = new QueryThingAction();
        TA.setIo(sys.io).setThingSet(hc.oRefer);
        TA.setQuery(tq);
        ThQr qr = TA.invoke();
        hc.pager = qr.pager;
        hc.output = qr.data;

    }

}
