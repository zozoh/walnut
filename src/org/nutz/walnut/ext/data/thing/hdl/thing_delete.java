package org.nutz.walnut.ext.data.thing.hdl;

import java.util.List;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.WnThingService;
import org.nutz.walnut.ext.data.thing.util.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cnqihbslVNHQ", regex = "^(hard|quiet)$")
public class thing_delete implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 分析参数
        boolean hard = hc.params.is("hard");
        Object match = hc.params.get("match");

        // 准备服务类
        WnObj oTs = Things.checkThingSet(hc.oRefer);
        WnThingService wts = new WnThingService(sys, oTs);

        // 调用接口
        List<WnObj> list = wts.deleteThing(sys, match, hard, hc.params.vals);
        if (hc.params.is("l") || list.size() > 1) {
            hc.output = list;
        }
        // 只输出一个
        else if (1 == list.size()) {
            hc.output = list.get(0);
        }
    }

}
