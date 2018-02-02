package org.nutz.walnut.ext.ticket;

import org.nutz.lang.Lang;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_ticket extends JvmHdlExecutor {
    @Override
    protected void _before_invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 尝试读取管道内的数据添加到当前的参数中
        NutMap vars = new NutMap();
        if (sys.pipeId > 0) {
            vars = Lang.map(sys.in.readAll());
        }
        if (!vars.isEmpty() && hc.args.length > 0) {
            for (int i = 0; i < hc.args.length; i++) {
                hc.args[i] = Tmpl.exec(hc.args[i], vars);
            }
        }
        super._before_invoke(sys, hc);
    }
}