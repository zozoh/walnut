package org.nutz.walnut.ext.sys.task.hdl;

import java.util.List;
import org.nutz.json.Json;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.sys.task.TaskCtx;
import org.nutz.walnut.ext.sys.task.WnTaskTable;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class Th_query extends AbstractTaskQueryHdl {

    @Override
    public void invoke(WnSystem sys, TaskCtx sc) throws Exception {
        // 解析参数
        ZParams params = ZParams.parse(sc.args, "^json|nocolor$");

        // 准备查询条件
        WnQuery q = new WnQuery();
        boolean logic_order = _fill_query(sys, sc, params, q);
        boolean nocolor = params.is("nocolor");

        // 查询出结果
        List<WnObj> list = sys.io.query(q);

        // ........................................................
        // 整理逻辑顺序
        if (logic_order) {
            list = _sort_by_logic_order(list);
        }

        // ........................................................
        // 最后输出
        if (params.is("json")) {
            sys.out.println(Json.toJson(list));
        }
        // 默认按行输出
        else {
            WnTaskTable wtt = new WnTaskTable("id,status,ow,cmtnb,title");
            for (WnObj o : list)
                wtt.add(0, o, nocolor);

            // 最终输出
            sys.out.print(wtt.toString());
        }

    }

}
