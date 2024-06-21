package com.site0.walnut.ext.data.sqlx.hdl;

import java.util.Date;

import org.nutz.lang.util.NutBean;
import org.nutz.mapl.Mapl;

import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.val.SeqMaker;
import com.site0.walnut.val.SeqMakerBuilder;
import com.site0.walnut.val.ValueMaker;
import com.site0.walnut.val.ValueMakers;
import com.site0.walnut.val.util.WnSeqInfo;

public class sqlx_set extends SqlxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(explain)$");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        String varName = params.val_check(0);
        String varValue = params.val_check(1);
        String to = params.getString("to", "all");

        // 获取值的生成器
        ValueMaker vmk = ValueMakers.build(varValue, new SeqMakerBuilder() {
            public SeqMaker build(WnSeqInfo info) {
                return ValueMakers.getSeqMaker(sys, info);
            }
        });

        // 根据模式执行
        if ("all".equals(to)) {
            forList(fc, varName, vmk);
            forMap(fc, varName, vmk);
        }
        // 仅列表
        else if ("list".equals(to)) {
            forList(fc, varName, vmk);
        }
        // 仅映射
        else if ("map".equals(to)) {
            forMap(fc, varName, vmk);
        }

        if (params.is("explain")) {
            fc.explainVars();
        }
    }

    private void forList(SqlxContext fc, String varName, ValueMaker vmk) {
        if (!fc.hasVarList()) {
            fc.resetVarList();
        }
        // 获取上一次更新的结果
        NutBean ctx = fc.prepareResultBean();

        // 循环滚动
        for (NutBean bean : fc.getVarList()) {
            Object v = vmk.make(new Date(), ctx);
            Mapl.put(bean, varName, v);
        }
    }

    private void forMap(SqlxContext fc, String varName, ValueMaker vmk) {

        if (!fc.hasVarMap()) {
            fc.resetVarMap();
        }
        // 获取上一次更新的结果
        NutBean ctx = fc.prepareResultBean();
        NutBean bean = fc.getVarMap();

        Object v = vmk.make(new Date(), ctx);
        Mapl.put(bean, varName, v);
    }

}
