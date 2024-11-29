package com.site0.walnut.ext.data.sqlx.hdl;

import java.util.Date;

import org.nutz.lang.util.NutBean;
import org.nutz.mapl.Mapl;

import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.tmpl.WnTmplX;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;
import com.site0.walnut.val.ValueMaker;
import com.site0.walnut.val.ValueMakers;

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
        String[] alias = params.getAs("alias", String[].class);
        WnMatch when = null;
        if (params.has("when")) {
            when = AutoMatch.parse(params.get("when"));
        }
        WnTmplX savepipe = null;
        if (params.has("savepipe")) {
            savepipe = WnTmplX.parse(params.getString("savepipe"));
        }

        // 获取值的生成器
        ValueMaker vmk = ValueMakers.build(sys, varValue);

        // 根据模式执行
        if ("all".equals(to)) {
            forList(fc, varName, vmk, savepipe, alias, when);
            forMap(fc, varName, vmk, savepipe, alias, when);
        }
        // 仅列表
        else if ("list".equals(to)) {
            forList(fc, varName, vmk, savepipe, alias, when);
        }
        // 仅映射
        else if ("map".equals(to)) {
            forMap(fc, varName, vmk, savepipe, alias, when);
        }

        if (params.is("explain")) {
            fc.explainVars();
        }
    }

    private void forList(SqlxContext fc,
                         String varName,
                         ValueMaker vmk,
                         WnTmplX savepipe,
                         String[] alias,
                         WnMatch when) {
        if (!fc.hasVarList()) {
            fc.resetVarList();
        }
        // 获取上一次更新的结果
        NutBean ctx = fc.getPipeContext();

        // 循环滚动
        for (NutBean bean : fc.getVarList()) {
            Object v = vmk.make(new Date(), ctx);
            Mapl.put(bean, varName, v);

            if (null != savepipe) {
                String pipeKey = savepipe.render(bean);
                fc.putPipeContext(pipeKey, v);
            }

            if (null != alias && alias.length > 0) {
                if (null == when || when.match(bean)) {
                    for (String a : alias) {
                        Mapl.put(bean, a, v);
                    }
                }
            }

        }
    }

    private void forMap(SqlxContext fc,
                        String varName,
                        ValueMaker vmk,
                        WnTmplX savepipe,
                        String[] alias,
                        WnMatch when) {

        if (!fc.hasVarMap()) {
            fc.resetVarMap();
        }
        // 获取上一次更新的结果
        NutBean ctx = fc.getPipeContext();
        NutBean bean = fc.getVarMap();

        Object v = vmk.make(new Date(), ctx);
        Mapl.put(bean, varName, v);

        if (null != savepipe) {
            String pipeKey = savepipe.render(bean);
            fc.putPipeContext(pipeKey, v);
        }

        if (null != alias && alias.length > 0) {
            if (null == when || when.match(bean)) {
                for (String a : alias) {
                    Mapl.put(bean, a, v);
                }
            }
        }
    }

}
