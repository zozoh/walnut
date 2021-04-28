package org.nutz.walnut.ext.data.o.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class o_tab extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "bish");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 准备输出字段
        List<String> list = new LinkedList<>();
        for (String val : params.vals) {
            String[] ss = Ws.splitIgnoreBlank(val);
            for (String s : ss) {
                list.add(s);
            }
        }

        if (list.size() > 0) {
            String[] cols = new String[list.size()];
            list.toArray(cols);
            // 准备参数
            boolean showBorder = params.is("b");
            boolean showHeader = params.is("h");
            boolean showSummary = params.is("s");
            boolean showIndex = params.is("i");
            int indexBase = params.getInt("ibase", 0);

            // 输出
            Cmds.output_objs_as_table(sys,
                                      fc.pager,
                                      fc.list,
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
