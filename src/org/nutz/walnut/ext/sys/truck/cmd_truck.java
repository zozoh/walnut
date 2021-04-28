package org.nutz.walnut.ext.sys.truck;

import java.util.LinkedList;

import org.nutz.lang.tmpl.Tmpl;
import org.nutz.walnut.ext.sys.truck.impl.TruckPrinter;
import org.nutz.walnut.ext.sys.truck.impl.WnTruckService;
import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.WnPager;
import org.nutz.walnut.util.ZParams;

public class cmd_truck extends JvmFilterExecutor<TruckContext, TruckFilter> {

    // private static final Log log = Logs.get();

    public cmd_truck() {
        super(TruckContext.class, TruckFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnbish", "^(quiet|ajax|json)$");
    }

    @Override
    protected TruckContext newContext() {
        return new TruckContext();
    }

    @Override
    protected void prepare(WnSystem sys, TruckContext tc) {
        // 设置 IO 接口
        tc.io = sys.io;

        // 非静默模式，准备收集输出集合
        if (!tc.params.is("quiet")) {
            // 如果仅仅是模板输出模式，也没必要收集列表，会太占内存了
            if (tc.params.has("tmpl")) {
                Tmpl tmpl = Cmds.parse_tmpl(tc.params.get("tmpl"));
                tc.printer = new TruckPrinter(sys.out, tmpl);
            }
            // 收集准备返回
            else {
                tc.list = new LinkedList<>();
            }
        }

    }

    @Override
    protected void output(WnSystem sys, TruckContext tc) {
        // 执行转换
        WnTruckService truck = new WnTruckService();
        truck.lanuch(tc);

        // 输出内容
        if (null != tc.list) {
            WnPager wp = new WnPager(tc.limit, tc.skip);
            Cmds.output_objs(sys, tc.params, wp, tc.list, false);
        }
    }

}
