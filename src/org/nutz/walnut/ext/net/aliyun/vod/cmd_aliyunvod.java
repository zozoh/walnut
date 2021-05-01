package org.nutz.walnut.ext.net.aliyun.vod;

import org.nutz.walnut.ext.net.aliyun.JvmAliyunExecutor;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_aliyunvod extends JvmAliyunExecutor {

    @Override
    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        this._findHdlNameBy(sys, hc, "vod", WnAliyunVodConf.class);
    }

}
