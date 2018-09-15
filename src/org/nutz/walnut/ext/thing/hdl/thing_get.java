package org.nutz.walnut.ext.thing.hdl;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.WnThingService;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cnqihbslVNHQ")
public class thing_get implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 分析参数
        String thId = hc.params.val_check(0);
        boolean isFull = hc.params.is("full");
        String sort = hc.params.get("sort");
        String sortKey = null;
        boolean isAsc = true;
        if (!Strings.isBlank(sort) && !"true".equals(sort)) {
            int pos = sort.indexOf(':');
            if (pos > 0) {
                sortKey = sort.substring(0, pos);
                isAsc = !sort.substring(pos + 1).equals("desc");
            }
            // 否则用默认
            else {
                sortKey = sort;
                isAsc = true;
            }
        }

        // 准备服务类
        WnObj oTs = Things.checkThingSet(hc.oRefer);
        WnThingService wts = new WnThingService(sys, oTs);

        // 调用接口
        hc.output = wts.getThing(thId, isFull, sortKey, isAsc);

    }

}
