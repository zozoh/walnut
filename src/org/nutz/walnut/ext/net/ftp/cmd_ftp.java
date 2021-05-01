package org.nutz.walnut.ext.net.ftp;

import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_ftp extends JvmHdlExecutor {

    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        _find_hdl_name_with_conf(sys, hc, "ftp", "ftpconf");
    }
}
