package org.nutz.walnut.ext.data.wf.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.data.wf.WfContext;
import org.nutz.walnut.ext.data.wf.WfFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

public class wf_view extends WfFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn");
    }

    @Override
    protected void process(WnSystem sys, WfContext fc, ZParams params) {
        // 分析参数
        NutMap ons = new NutMap();
        for (String v : params.vals) {
            ons.put(v, true);
        }
        boolean viewAll = ons.isEmpty();
        boolean viewVars = ons.getBoolean("vars");
        boolean viewData = ons.getBoolean("data");
        boolean viewNode = ons.getBoolean("node");
        boolean viewEdge = ons.getBoolean("edge");

        // 准备返回值
        NutMap re = new NutMap();

        // 上下文变量
        if (viewAll || viewVars) {
            re.put("vars", fc.vars);
        }

        // 工作流节点
        if (viewAll || viewData || viewNode) {
            if (fc.hasWorkflow()) {
                re.put("nodes", fc.workflow.getNodes());
            } else {
                re.put("nodes", "!UNDEFINED!");
            }
        }

        // 工作流边
        if (viewAll || viewData || viewEdge) {
            if (fc.hasWorkflow()) {
                re.put("edges", fc.workflow.getEdges());
            } else {
                re.put("edges", "!UNDEFINED!");
            }
        }

        // 输出
        JsonFormat jfmt = Cmds.gen_json_format(params);
        String json = Json.toJson(re, jfmt);
        sys.out.println(json);
    }

}
