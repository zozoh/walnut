package com.site0.walnut.ext.net.ftp;

import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlExecutor;
import com.site0.walnut.impl.box.WnSystem;

public class cmd_ftp extends JvmHdlExecutor {

    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        _find_hdl_name_with_conf(sys, hc, "ftp", "ftpconf");
    }
}
