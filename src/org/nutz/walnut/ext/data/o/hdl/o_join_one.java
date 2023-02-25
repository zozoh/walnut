package org.nutz.walnut.ext.data.o.hdl;

import java.util.List;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
import org.nutz.walnut.ext.data.o.util.WnObjJoin;
import org.nutz.walnut.ext.data.o.util.WnObjTrans;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wcol;
import org.nutz.walnut.util.ZParams;

public class o_join_one extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(only)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 分析参数
        WnObjJoin join = new WnObjJoin(sys);
        join.loadFrom(params);
        join.setLimit(1);

        WnObjTrans trans = new WnObjTrans();
        trans.loadFrom(sys, params);
        join.setTrans(trans);

        if (!fc.list.isEmpty()) {
            for (WnObj obj : fc.list) {
                join_one(join.clone(), obj);
            }
        }
    }

    protected void join_one(WnObjJoin join, WnObj obj) {
        // 准备对象
        join.loadObjAndParent(obj);

        // 查询数据
        List<Object> list = join.queryAndTrans();
        Object ta = Wcol.first(list);

        // 设置值
        join.joinToObj(ta);
    }

}
