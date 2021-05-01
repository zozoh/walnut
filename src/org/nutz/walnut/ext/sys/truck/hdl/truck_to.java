package org.nutz.walnut.ext.sys.truck.hdl;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sys.truck.TruckContext;
import org.nutz.walnut.ext.sys.truck.TruckFilter;
import org.nutz.walnut.ext.sys.truck.TruckMode;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class truck_to extends TruckFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(noexists|genid)$");
    }

    @Override
    protected void process(WnSystem sys, TruckContext tc, ZParams params) {
        // 分析目标
        String dirPath = params.val(0);
        WnObj oDir;

        if (!Strings.isBlank(dirPath)) {
            oDir = Wn.checkObj(sys, dirPath);
        } else {
            oDir = tc.fromDir;
        }

        // 目标不能为空
        if (null == oDir) {
            throw Er.create("e.cmd.truck.to.withoutTarget");
        }

        // 准备转换模式
        String smode = params.get("mode", "INDEXER");

        // 设置参数
        tc.noexists = params.is("noexists");
        tc.genId = params.is("genid");

        // 设置到上下文
        tc.toDir = oDir;
        tc.mode = TruckMode.valueOf(smode);
    }

}
