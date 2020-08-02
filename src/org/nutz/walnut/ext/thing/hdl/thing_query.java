package org.nutz.walnut.ext.thing.hdl;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.WnThingService;
import org.nutz.walnut.ext.thing.util.ThQr;
import org.nutz.walnut.ext.thing.util.ThQuery;
import org.nutz.walnut.ext.thing.util.Things;
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
        // 分析参数
        ThQuery tq = new ThQuery();

        // ..............................................
        // 准备分页信息
        tq.wp = new WnPager(hc.params);

        // ..............................................
        // 准备查询条件
        tq.qStr = Cmds.getParamOrPipe(sys, hc.params, 0);

        // 如果还需要查询关联对象的内容指纹
        String sha1 = hc.params.getString("sha1");
        if (!Strings.isBlank(sha1)) {
            tq.sha1Fields = Strings.splitIgnoreBlank(sha1);
        }

        // ..............................................
        // 确保限定了集合
        tq.tss = hc.params.getAs("tss", String[].class);

        // 设置排序
        if (hc.params.hasString("sort")) {
            tq.sort = Lang.map(hc.params.check("sort"));
        }

        tq.needContent = hc.params.is("content");
        tq.autoObj = hc.params.is("obj");

        // 准备服务类
        WnThingService wts;
        // 指定了 ThingSet
        if (tq.tss != null && tq.tss.length > 0) {
            wts = new WnThingService(sys, null);
        }
        // 否则用自己当前目录作为 ThingSet
        else {
            WnObj oTs = Things.checkThingSet(hc.oRefer);
            wts = new WnThingService(sys, oTs);
        }

        // 调用接口
        ThQr qr = wts.queryThing(tq);

        hc.pager = qr.pager;
        hc.output = qr.data;
    }

}
