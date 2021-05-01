package org.nutz.walnut.ext.net.xapi;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

public class cmd_xapi extends JvmHdlExecutor {

    public static NutMap loadVars(WnSystem sys, JvmHdlContext hc) {
        NutMap vars = sys.session.getVars().duplicate();
        String str = Cmds.getParamOrPipe(sys, hc.params, "vars", true);
        if (!Strings.isBlank(str)) {
            NutMap v2 = Lang.map(str);
            vars.putAll(v2);
        }
        return vars;
    }

}
