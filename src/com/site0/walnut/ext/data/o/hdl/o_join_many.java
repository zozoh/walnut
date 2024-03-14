package com.site0.walnut.ext.data.o.hdl;

import java.util.List;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.ext.data.o.util.WnObjJoin;
import com.site0.walnut.ext.data.o.util.WnObjTrans;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class o_join_many extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(only|fetch)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 分析参数
        WnObjJoin join = new WnObjJoin(sys);
        join.loadFrom(params);

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

        // 设置值
        join.joinToObj(list);
    }

}
