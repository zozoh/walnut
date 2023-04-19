package org.nutz.walnut.ext.data.wf.hdl;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.data.wf.WfContext;
import org.nutz.walnut.ext.data.wf.WfFilter;
import org.nutz.walnut.ext.data.wf.bean.WfActionElement;
import org.nutz.walnut.ext.data.wf.bean.WfEdge;
import org.nutz.walnut.ext.data.wf.bean.WfNode;
import org.nutz.walnut.ext.util.react.action.ReactActionContext;
import org.nutz.walnut.ext.util.react.action.ReactActionHandler;
import org.nutz.walnut.ext.util.react.bean.ReactAction;
import org.nutz.walnut.ext.util.react.util.WnReacts;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class wf_process extends WfFilter {

    private static String AUTO_NEXT = "next";
    private static String AUTO_CURRENT = "current";

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(test|wary)$");
    }

    @Override
    protected void process(WnSystem sys, WfContext fc, ZParams params) {
        // 防守:是否加载了工作流
        if (!fc.hasWorkflow()) {
            return;
        }

        // 分析参数
        boolean isTest = params.is("test");
        boolean isWary = params.is("wary");
        String autoMode = params.getString("auto", AUTO_NEXT);

        // 防守:是否确认了开始节点
        WfNode node = null;
        String cuName;
        if (!fc.hasCurrentName()) {
            // 谨慎模式下，则直接退出执行
            if (isWary) {
                return;
            }
            // 寻找起始节点
            String headName = fc.workflow.findStartNodeName();
            if (null == headName) {
                return;
            }
            // 设置 NEXT_NAME
            if (AUTO_NEXT.equals(autoMode)) {
                fc.setNextName(headName);
                cuName = headName;
                // 执行节点
                if (!isTest) {
                    node = fc.workflow.getNode(headName);
                    this.processWfActionElement(sys, fc, node);
                    // 自动找到一个节点并执行，要不要后续再执行呢？
                    // 这里看一下，是否有 auto=next 标记就好了
                    if (null == node || node.isTAIL() || !node.isAutoNext()) {
                        return;
                    }
                }
            }
            // 设置 CURRENT_NEXT
            else if (AUTO_CURRENT.equals(autoMode)) {
                fc.setCurrentName(headName);
                cuName = headName;
                node = fc.workflow.getNode(cuName);
            }
            // 其他的就是瞎几把设，直接退出
            else {
                throw Er.create("e.cmd.wf_process.InvalidAutoMode", autoMode);
            }
        }
        // 准备开始节点
        else {
            cuName = fc.getCurrentName();
            node = fc.workflow.getNode(cuName);
        }

        // 循环执行，直到触达【状态/尾】节点，或者未找到连通边为止
        while (true) {
            // 【退出点】节点非法，或者已达尾部（防止小贱人乱设置 CURRENT_NAME）
            if (null == node || node.isTAIL() ) {
                return;
            }

            // 找到当前节点连通的边
            WfEdge edge = fc.workflow.tryEdge(cuName, fc.vars);

            // 【退出点】未找到了连通的边
            if (null == edge) {
                return;
            }

            // 找到目标节点
            String nextName = edge.getToName();
            WfNode taNode = fc.workflow.getNode(nextName);

            // 【退出点】未找到对应的节点
            if (null == taNode) {
                return;
            }

            // 设置上下文
            fc.setNextName(nextName);
            fc.setNextType(taNode.getType());

            // 执行动作项
            if (!isTest) {
                // 首先执行边动作
                this.processWfActionElement(sys, fc, edge);

                // 其次执行节点动作
                this.processWfActionElement(sys, fc, taNode);
            }

            // 【退出点】已经是一个状态节点
            // 状态节点相当于 yield，那么整个处理进程则需挂起
            if (taNode.isSTATE() && !taNode.isAutoNext()) {
                return;
            }

            // 继续下一个循环
            cuName = nextName;
            node = fc.workflow.getNode(cuName);
        }

    }

    private void processWfActionElement(WnSystem sys, WfContext fc, WfActionElement ae) {
        if (ae.hasActions()) {
            // 准备上下文
            ReactActionContext r = new ReactActionContext();
            r.vars = fc.vars;
            r.runner = sys;
            r.io = sys.io;
            r.session = sys.session;
            // 执行
            doActions(ae.getActions(), r);
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
