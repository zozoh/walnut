package com.site0.walnut.ext.sys.refer;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

public class cmd_refer extends JvmFilterExecutor<ReferContext, ReferFilter> {

    public cmd_refer() {
        super(ReferContext.class, ReferFilter.class);
    }

    @Override
    protected ReferContext newContext() {
        return new ReferContext();
    }

    @Override
    protected void prepare(WnSystem sys, ReferContext fc) {
        // 读取配置映射对象
        String str = fc.params.val(0);
        if (!Ws.isBlank(str)) {
            fc.oDir = Wn.checkObj(sys, str);
            // 检查 mount 信息
            if (Ws.isBlank(fc.oDir.mount())) {
                throw Er.create("e.cmd.refer.ObjWithoutMount", str);
            }
        }

    }

    @Override
    protected void output(WnSystem sys, ReferContext fc) {}

}
