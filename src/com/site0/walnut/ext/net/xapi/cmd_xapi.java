package com.site0.walnut.ext.net.xapi;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;

public class cmd_xapi extends JvmHdlExecutor {

    public static NutMap loadVars(WnSystem sys, JvmHdlContext hc) {
        NutMap vars = sys.session.getVars().duplicate();
        String str = Cmds.getParamOrPipe(sys, hc.params, "vars", true);
        if (!Strings.isBlank(str)) {
            NutMap v2 = Wlang.map(str);
            vars.putAll(v2);
        }
        return vars;
    }

}
