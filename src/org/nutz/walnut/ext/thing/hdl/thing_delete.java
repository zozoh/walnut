package org.nutz.walnut.ext.thing.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.WnThingService;
import org.nutz.walnut.ext.thing.util.Things;
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
        
        // 准备服务类
        WnObj oTs = Things.checkThingSet(hc.oRefer);
        WnThingService wts = new WnThingService(sys, oTs);
        
        // 调用接口
        hc.output = wts.deleteThing(hard, hc.params.vals);

    }

}
