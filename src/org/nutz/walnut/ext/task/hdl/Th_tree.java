package org.nutz.walnut.ext.task.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Each;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.task.TaskCtx;
import org.nutz.walnut.ext.task.WnTaskTable;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class Th_tree extends AbstractTaskQueryHdl {

    @Override
    public void invoke(WnSystem sys, TaskCtx sc) throws Exception {

        // 首先准备要处理的任务列表
        final List<WnObj> list = new LinkedList<WnObj>();

        // 准备参数
        ZParams params = ZParams.parse(sc.args, "^json|nocolor$");
        WnQuery q = new WnQuery();
        boolean logic_order = this._fill_query(sys, sc, params, q);
        boolean nocolor = params.is("nocolor");

        // 指定任务
        if (null != sc.oTask) {
            list.add(sc.oTask);
        }
        // 搜索根任务
        else {
            WnObj oParent = sys.io.fetch(sc.oTaskHome, "root");
            q.setv("pid", oParent.id());
            sys.io.each(q, new Each<WnObj>() {
                public void invoke(int index, WnObj o, int length) {
                    if (o.isType("task"))
                        list.add(o);
                }
            });
        }

        // 循环处理
        for (WnObj task : list) {
            __eval_tasks(sys, sc, q, logic_order, task);
        }

        // 最后输出
        // 如果是 json 方式输出
        if (params.is("json")) {
            sys.out.println(Json.toJson(list));
        }
        // 否则按行输出
        else {
            WnTaskTable wtt = new WnTaskTable("id,status,ow,cmtnb,title");
            for (WnObj task : list)
                __join_task_to_table(wtt, task, 0, nocolor);

            // 最终输出
            sys.out.print(wtt.toString());
        }
    }

    private void __join_task_to_table(WnTaskTable wtt, WnObj task, int indent, boolean nocolor) {
        wtt.add(indent, task, nocolor);
        List<WnObj> children = task.getList("children", WnObj.class);
        if (null != children) {
            for (WnObj child : children) {
                __join_task_to_table(wtt, child, indent + 1, nocolor);
            }
        }

    }

    private void __eval_tasks(WnSystem sys,
                              TaskCtx sc,
                              WnQuery q,
                              boolean logic_order,
                              WnObj task) {
        // 查询子
        q.setv("pid", task.id());
        List<WnObj> children = sys.io.query(q);

        // 逻辑排序
        if (logic_order)
            children = _sort_by_logic_order(children);

        // 递归
        for (WnObj child : children) {
            __eval_tasks(sys, sc, q, logic_order, child);
        }

        // 记录
        task.setv("children", children);
    }

}
