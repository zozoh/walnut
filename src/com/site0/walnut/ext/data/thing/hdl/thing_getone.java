package com.site0.walnut.ext.data.thing.hdl;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.thing.WnThingService;
import com.site0.walnut.ext.data.thing.util.ThQr;
import com.site0.walnut.ext.data.thing.util.ThQuery;
import com.site0.walnut.ext.data.thing.util.Things;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.WnPager;

@JvmHdlParamArgs(value = "cqn", regex = "^(content|obj)$")
public class thing_getone implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析参数
        ThQuery tq = new ThQuery();

        // ..............................................
        // 准备分页信息
        tq.wp = new WnPager(1, 0);

        // ..............................................
        // 准备查询条件
        tq.qStr = Cmds.getParamOrPipe(sys, hc.params, 0);

        // ..............................................
        // 设置排序
        if (hc.params.hasString("sort")) {
            tq.sort = Wlang.map(hc.params.check("sort"));
        }

        // 如果还需要查询关联对象的内容指纹
        String sha1 = hc.params.getString("sha1");
        if (!Strings.isBlank(sha1)) {
            tq.sha1Fields = Strings.splitIgnoreBlank(sha1);
        }

        tq.needContent = hc.params.is("content");
        tq.autoObj = true;

        // 准备服务类
        WnThingService wts;
        WnObj oTs = Things.checkThingSet(hc.oRefer);
        wts = new WnThingService(sys, oTs);

        // 调用接口
        ThQr qr = wts.queryThing(tq);
        hc.output = qr.data;
    }

}
