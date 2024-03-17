package com.site0.walnut.ext.data.sqlx.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class sqlx_tab extends SqlxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "bish");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        // 准备输出字段
        List<String> fields = new LinkedList<>();
        for (String val : params.vals) {
            String[] ss = Ws.splitIgnoreBlank(val);
            for (String s : ss) {
                fields.add(s);
            }
        }

        List<NutMap> list = Wlang.anyToList(fc.result, NutMap.class);

        if (fields.size() > 0) {
            String[] cols = new String[fields.size()];
            fields.toArray(cols);
            // 准备参数
            boolean showBorder = params.is("b");
            boolean showHeader = params.is("h");
            boolean showSummary = params.is("s");
            boolean showIndex = params.is("i");
            int indexBase = params.getInt("ibase", 0);

            // 输出
            Cmds.output_objs_as_table(sys,
                                      null,
                                      list,
                                      cols,
                                      showBorder,
                                      showHeader,
                                      showSummary,
                                      showIndex,
                                      indexBase);
        }

        fc.quiet = true;
    }

}
