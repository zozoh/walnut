package org.nutz.walnut.ext.data.wf.hdl;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.data.wf.WfContext;
import org.nutz.walnut.ext.data.wf.WfFilter;
import org.nutz.walnut.ext.data.wf.bean.WfEdge;
import org.nutz.walnut.ext.data.wf.bean.WfNode;
import org.nutz.walnut.ext.util.react.action.ReactActionContext;
import org.nutz.walnut.ext.util.react.action.ReactActionHandler;
import org.nutz.walnut.ext.util.react.bean.ReactAction;
import org.nutz.walnut.ext.util.react.util.WnReacts;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class wf_process extends WfFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(test)$");
    }

    @Override
    protected void process(WnSystem sys, WfContext fc, ZParams params) {
        // 防守:是否加载了工作流
        if (!fc.hasWorkflow()) {
            return;
        }
        // 防守:是否确认了开始节点
        if (!fc.hasCurrentName()) {
            return;
        }
        // 分析参数
        boolean isTest = params.is("test");

        // 准备开始节点
        String cuName = fc.getCurrentName();

        // 找到当前节点连通的边
        WfEdge edge = fc.workflow.tryEdge(cuName, fc.vars);

        // 未找到了连通的边
        if (null == edge) {
            return;
        }

        // 找到目标节点
        String nextName = edge.getToName();
        WfNode taNode = fc.workflow.getNode(nextName);

        // 未找到对应的节点
        if (null == taNode) {
            return;
        }

        // 设置上下文
        fc.setNextName(nextName);

        // 执行动作项
        if (!isTest) {
            // 准备上下文
            ReactActionContext r = new ReactActionContext();
            r.vars = fc.vars;
            r.runner = sys;
            r.io = sys.io;
            r.session = sys.session;
            // 边的动作项
            if (edge.hasActions()) {
                doActions(edge.getActions(), r);
            }
            // 节点的动作项
            if (taNode.hasActions()) {
                doActions(taNode.getActions(), r);
            }
        }

    }

    private void doActions(ReactAction[] actions, ReactActionContext r) {
        for (ReactAction a : actions) {
            // 获取执行器
            ReactActionHandler hdl = WnReacts.getActionHandler(a);
            if (null == hdl) {
                throw Er.create("e.cmd.wf.process.ActionNotFound", a.toString());
            }

            // 根据上下文，处理动作对象属性
            a.explain(r.vars);

            // 执行
            hdl.run(r, a);
        }
    }

}
