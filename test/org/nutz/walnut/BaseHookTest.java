package org.nutz.walnut;

import org.nutz.ioc.impl.PropertiesProxy;
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
    protected void on_before(PropertiesProxy pp) {
        super.on_before(pp);

        // 检查钩子目录
        oHome = io.check(null, me.home());
        oHookHome = io.createIfNoExists(oHome, ".hook", WnRace.DIR);

        // 准备钩子上下文
        WnBoxContext bc = new WnBoxContext();
        bc.io = io;
        bc.me = me;
        bc.session = se;
        bc.usrService = usrs;
        bc.sessionService = ses;

        hc = new WnHookContext(boxes, bc);
        hc.io = io;
        hc.me = me;
        hc.se = se;
        hc.service = new CachedWnHookService().setIo(io);
    }

    @Override
    protected void on_after(PropertiesProxy pp) {
        super.on_after(pp);
    }

}
