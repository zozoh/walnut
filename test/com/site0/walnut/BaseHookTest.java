package com.site0.walnut;

import com.site0.walnut.api.hook.WnHookContext;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.core.io.WnIoHookedWrapper;
import com.site0.walnut.impl.hook.CachedWnHookService;

public abstract class BaseHookTest extends BaseBoxTest {

    protected WnObj oHome;

    protected WnObj oHookHome;

    protected WnHookContext hc;

    @Override
    protected void on_before() {
        super.on_before();

        // 检查钩子目录
        oHome = io.check(null, me.getHomePath());
        oHookHome = io.createIfNoExists(oHome, ".hook", WnRace.DIR);

        // 准备钩子上下文
        hc = new WnHookContext(boxes, bc);
        hc.service = new CachedWnHookService().setIo(io);

        //System.out.printf("\nTestCase: %s\n", hc.service.toString());
    }

    // 改用钩子版的 IO
    @Override
    protected WnIo prepareIo() {
        return new WnIoHookedWrapper(setup.getIo());
    }

    @Override
    protected void on_after() {
        super.on_after();
    }

}
