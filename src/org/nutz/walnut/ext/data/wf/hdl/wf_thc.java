package org.nutz.walnut.ext.data.wf.hdl;

import java.util.List;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.WnThingService;
import org.nutz.walnut.ext.data.wf.WfContext;
import org.nutz.walnut.ext.data.wf.WfFilter;
import org.nutz.walnut.ext.data.wf.util.Wfs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class wf_thc extends WfFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(force)$");
    }

    @Override
    protected void process(WnSystem sys, WfContext fc, ZParams params) {
        // 至少要有两个参数吧
        if (params.vals.length < 2) {
            return;
        }

        // 防守: 判断条件
        if (!isCanGoingOn(fc, params)) {
            return;
        }

        // 分析参数
        String ph = params.val_check(0);
        String json = params.val_check(1);
        String asName = params.getString("as");
        String ukey = params.getString("uniq", null);
        NutMap fixedMeta = params.getMap("fixed");
        boolean force = params.is("force");

        // 前序检查
        if (!force && !fc.hasNextName()) {
            return;
        }

        // 得到对象元数据或者元数据列表
        Object input = Json.fromJson(json);
        Wfs.AtmlResult ar = Wfs.anyToMetaList(input);

        // 再防守一道
        if (ar.list.isEmpty()) {
            return;
        }

        // 准备 Thing 的服务类
        WnObj oTs = Wn.checkObj(sys, ph);
        WnThingService wts = new WnThingService(sys, oTs);

        // 循环创建
        List<WnObj> results = wts.createThings(ar.list, ukey, fixedMeta, sys.out, null, sys, null);

        // 记入上下文
        if (!Ws.isBlank(asName)) {
            // 存为列表
            if (ar.asList) {
                fc.vars.put(asName, results);
            }
            // 单个对象
            else {
                fc.vars.put(asName, results.get(0));
            }
        }
    }

}
