package com.site0.walnut.ext.data.thing.hdl;

import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.data.thing.WnThingService;
import com.site0.walnut.ext.data.thing.util.Things;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

@JvmHdlParamArgs(value = "cnqihbslVNHQ", regex = "^(hard|quiet)$")
public class thing_delete implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 分析参数
        boolean hard = hc.params.is("hard");
        Object match = hc.params.get("match");
        String qjson = hc.params.get("query");

        // 准备服务类
        WnObj oTs = Things.checkThingSet(hc.oRefer);
        WnThingService wts = new WnThingService(sys, oTs);

        // 调用接口
        List<WnObj> list;

        // 指定了 ID 的确定删除
        if (hc.params.vals.length > 0) {
            list = wts.deleteThing(sys, match, hard, hc.params.vals);
        }
        // 泛条件删除
        else if (!Ws.isBlank(qjson)) {
            int safeCount = hc.params.getInt("safec", 0);
            WnQuery q = Wn.Q.jsonToQuery(qjson);
            list = wts.deleteThing(sys, q, safeCount, match, hard);
        }
        // 什么都不做
        else {
            list = new LinkedList<>();
        }

        if (hc.params.is("l") || list.size() > 1) {
            hc.output = list;
        }
        // 只输出一个
        else if (1 == list.size()) {
            hc.output = list.get(0);
        }
    }

}
