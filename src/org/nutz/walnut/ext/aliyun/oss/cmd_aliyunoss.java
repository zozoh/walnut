package org.nutz.walnut.ext.aliyun.oss;

import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_aliyunoss extends JvmHdlExecutor {

	protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        _find_hdl_name_with_conf(sys, hc, "aliyun/oss", "conf");
    }
}
