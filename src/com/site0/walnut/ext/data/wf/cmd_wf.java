package com.site0.walnut.ext.data.wf;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class cmd_wf extends JvmFilterExecutor<WfContext, WfFilter> {

    public cmd_wf() {
        super(WfContext.class, WfFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnl");
    }

    @Override
    protected WfContext newContext() {
        return new WfContext();
    }

    @Override
    protected void prepare(WnSystem sys, WfContext fc) {
        // 获取文件的 Key
        String getBy = fc.params.getString("get");
        // 尝试从文件读取
        if (fc.params.vals.length > 0) {
            for (int i = 0; i < fc.params.vals.length; i++) {
                String ph = fc.params.val(i);
                if (fc.tryLoadWorkflowFromObj(ph, getBy)) {
                    return;
                }
            }
            throw Er.create("e.cmd.wf.failToFoundWf", Ws.join(fc.params.vals, "; "));
        }
        // 从标准输入读取工作流
        else {
            String json = sys.in.readAll();
            fc.input = Json.fromJson(NutMap.class, json);
            fc.loadWorkflow(fc.input, getBy);
        }
    }

    @Override
    protected void output(WnSystem sys, WfContext fc) {}
}
