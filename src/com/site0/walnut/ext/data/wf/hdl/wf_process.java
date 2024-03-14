package com.site0.walnut.ext.data.wf.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.data.wf.WfContext;
import com.site0.walnut.ext.data.wf.WfFilter;
import com.site0.walnut.ext.data.wf.bean.WfActionElement;
import com.site0.walnut.ext.data.wf.bean.WfEdge;
import com.site0.walnut.ext.data.wf.bean.WfNode;
import com.site0.walnut.ext.util.react.action.ReactActionContext;
import com.site0.walnut.ext.util.react.action.ReactActionHandler;
import com.site0.walnut.ext.util.react.bean.ReactAction;
import com.site0.walnut.ext.util.react.util.WnReacts;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class wf_process extends WfFilter {

    private static String AUTO_NEXT = "next";
    private static String AUTO_CURRENT = "current";

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(test|wary|redo)$");
    }

    @Override
    protected void process(WnSystem sys, WfContext fc, ZParams params) {
        // 防守:是否加载了工作流
        if (!fc.hasWorkflow()) {
            return;
        }

        // 分析参数
        boolean isTest = params.is("test");
        boolean isMock = params.is("mock");
        boolean isWary = params.is("wary");
        String autoMode = params.getString("auto", AUTO_NEXT);
        boolean isRedo = params.is("redo");

        if (isMock) {
            fc.setBreakExec(true);
        }
        // 防守:是否确认了开始节点
        WfNode node = null;
        String cuName;

        // 重入模式，必须指定一个有效的当前节点
        if (isRedo) {
            if (fc.hasCurrentName()) {
                cuName = fc.getCurrentName();
                node = fc.workflow.getNode(cuName);
            } else {
                throw Er.create("e.wf.NeedNodeName");
            }
            // 未找到节点，抛个错
            if (null == node) {
                throw Er.create("e.wf.InvalidNodeName", cuName);
            }

            // 设置上下文
            fc.setNextName(cuName);
            fc.setNextType(node.getType());

            // 执行节点
            if (!isTest) {
                this.processWfActionElement(sys, fc, node, isMock);
            }

            // 重入模式就不尝试边了
            return;
        }

        // 没有当前节点，则看自动模式是什么
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
            // 设置 NEXT_NAME，如果是
            if (AUTO_NEXT.equals(autoMode)) {
                fc.setNextName(headName);
                cuName = headName;
                // 执行节点
                if (!isTest) {
                    node = fc.workflow.getNode(headName);
                    if (node != null) {
                        fc.setNextType(node.getType());
                        this.processWfActionElement(sys, fc, node, isMock);
                    }
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
            if (null == node || node.isTAIL()) {
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

            // 每次执行之前，确保重新加载了动态变量
            fc.reloadVars();

            // 设置上下文
            fc.setNextName(nextName);
            fc.setNextType(taNode.getType());

            // 执行动作项
            if (!isTest) {
                // 首先执行边动作
                this.processWfActionElement(sys, fc, edge, isMock);
                // 每次执行之前，确保重新加载了动态变量
                if (edge.hasActions()) {
                    fc.reloadVars();
                }
                // 其次执行节点动作
                this.processWfActionElement(sys, fc, taNode, isMock);
            }

            // 【退出点】不是结束，而且也不需要自动尝试
            // 相当于 yield，那么整个处理进程则需挂起
            if (taNode.isTAIL()) {
                return;
            }
            if (!taNode.isAutoNext()) {
                return;
            }

            // 继续下一个循环
            cuName = nextName;
            node = taNode;
        }

    }

    private void processWfActionElement(WnSystem sys, WfContext fc, WfActionElement ae, boolean isMock) {
        if (ae.hasActions()) {
            // 准备上下文
            ReactActionContext r = new ReactActionContext();
            r.vars = fc.vars;
            r.runner = sys;
            r.out = sys.out;
            r.io = sys.io;
            r.session = sys.session;
            // 执行
            doActions(ae.getActions(), r, isMock);
        }
    }

    private void doActions(ReactAction[] actions, ReactActionContext r, boolean isMock) {
        for (int i = 0; i < actions.length; i++) {
            ReactAction a = actions[i];
            // 获取执行器
            ReactActionHandler hdl = WnReacts.getActionHandler(a);
            if (null == hdl) {
                throw Er.create("e.cmd.wf.process.ActionNotFound", a.toString());
            }

            // 根据上下文，处理动作对象属性
            a.explain(r.vars);

            if (isMock) {
                String json = Json.toJson(a, JsonFormat.nice().setIgnoreNull(false));
                r.out.printlnf("action[%s] = %s", i, json);
            } else {
                // 执行
                hdl.run(r, a);
            }
        }
    }

}
