package org.nutz.walnut.ext.data.thing.hdl;

import java.util.List;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.WnThingService;
import org.nutz.walnut.ext.data.thing.util.ThingDuplicateOptions;
import org.nutz.walnut.ext.data.thing.util.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Ws;

@JvmHdlParamArgs(value = "cqn", regex = "(shallow|obj|nofiles)")
public class thing_duplicate implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 分析参数
        String thId = hc.params.val_check(0);
        ThingDuplicateOptions opt = new ThingDuplicateOptions();
        opt.dupCount = hc.params.val_int(1, 1);
        opt.toKey = hc.params.getString("tokey", "id");
        opt.fieldFilter = hc.params.getString("fields", null);
        opt.shallow = hc.params.is("shallow");
        opt.fixedMeta = hc.params.getMap("meta");

        // 指定复制目标
        if (hc.params.has("to")) {
            String toIds = hc.params.getString("to");
            opt.toIds = Ws.splitIgnoreBlanks(toIds);
        }

        // 准备服务类
        WnObj oTs = Things.checkThingSet(hc.oRefer);
        WnThingService wts = new WnThingService(sys, oTs);

        // 调用接口
        List<WnObj> list = wts.duplicateThing(sys, thId, opt);

        // 准备输出
        if (hc.params.is("obj") || list.size() == 1) {
            hc.output = list.get(0);
        }
        // 只输出一个
        else {
            hc.output = list;
        }
    }

}
