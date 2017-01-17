package org.nutz.walnut.ext.wup.hdl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

/**
 * 执行一个构建
 * 
 * @author wendal
 *
 */
public class wup_pkg_build implements JvmHdl {

    private static final Log log = Logs.get();
    
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        String app = hc.params.val_check(0);
        if (!app.matches("^([\\w]+)$")) {
            sys.err.println("not allow : " + app);
            return;
        }
        sys.out.println(">> build-" + app);
        try {
            sys.out.println(Lang.execOutput(new String[]{"build-" + app}));
        }
        catch (IOException e) {
            sys.err.println("build fail : " + app + " : " + e.getMessage());
            log.info(e.getMessage(), e);
        }
    }

}
