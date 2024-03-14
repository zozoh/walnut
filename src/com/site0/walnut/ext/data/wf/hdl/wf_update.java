package com.site0.walnut.ext.data.wf.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.wf.WfContext;
import com.site0.walnut.ext.data.wf.WfFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class wf_update extends WfFilter {

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
        boolean force = params.is("force");

        // 前序检查
        if (!force && !fc.hasNextName()) {
            return;
        }

        // 准备更新的元数据
        NutMap meta = Json.fromJson(NutMap.class, json);
        if (null == meta || meta.isEmpty()) {
            return;
        }
        // 解开更新宏
        Wn.explainMetaMacro(meta);

        // 准备目标对象
        WnObj oTa = Wn.checkObj(sys, ph);

        // 执行更新
        sys.io.appendMeta(oTa, meta);

        // 记入上下文
        if (!Ws.isBlank(asName)) {
            fc.vars.put(asName, oTa);
        }

    }

}
