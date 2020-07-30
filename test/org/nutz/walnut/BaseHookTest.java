package org.nutz.walnut;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.hook.WnHookContext;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.hook.CachedWnHookService;

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
        WnBoxContext bc = new WnBoxContext(new NutMap());
        bc.io = io;
        bc.session = se;
        bc.auth = auth;

        hc = new WnHookContext(boxes, bc);
        hc.service = new CachedWnHookService().setIo(io);

    }

    @Override
    protected void on_after() {
        super.on_after();
    }

}
