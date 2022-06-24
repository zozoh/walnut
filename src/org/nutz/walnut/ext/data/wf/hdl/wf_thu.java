package org.nutz.walnut.ext.data.wf.hdl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.WnThingService;
import org.nutz.walnut.ext.data.thing.util.Things;
import org.nutz.walnut.ext.data.wf.WfContext;
import org.nutz.walnut.ext.data.wf.WfFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class wf_thu extends WfFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(force)$");
    }

    @Override
    protected void process(WnSystem sys, WfContext fc, ZParams params) {
        // 至少要有三个参数吧
        if (params.vals.length < 3) {
            return;
        }

        // 防守: 判断条件
        if (!isCanGoingOn(fc, params)) {
            return;
        }

        boolean force = params.is("force");

        // 前序检查
        if (!force && !fc.hasNextName()) {
            return;
        }

        // 分析参数: 操作目标
        String ph = params.explainValAsString(fc.vars, 0);
        if (Ws.isBlank(ph)) {
            throw Er.create("e.wf.thu.WithoutTargetPh");
        }
        String id = params.explainValAsString(fc.vars, 1);
        if (Ws.isBlank(id)) {
            throw Er.create("e.wf.thu.WithoutTargetPh");
        }

        // 分析参数: 更新的元数据
        NutMap meta = params.explainValAsMap(fc.vars, 2);
        if (null == meta || meta.isEmpty()) {
            throw Er.create("e.wf.thu.EmptyMeta");
        }
        // 解开更新宏
        Things.formatMeta(meta);

        // 分析参数: 其他
        NutMap match = params.explainAsMap(fc.vars, "match");
        String asName = params.explainAsString(fc.vars, "as");

        // 准备 Thing 的服务类
        WnObj oTs = Wn.checkObj(sys, ph);
        WnThingService wts = new WnThingService(sys, oTs);

        // 执行更新
        WnObj oT = wts.updateThing(id, meta, sys, match);

        // 记入上下文
        if (!Ws.isBlank(asName)) {
            fc.vars.put(asName, oT);
        }
    }

}
