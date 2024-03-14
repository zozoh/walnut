package com.site0.walnut.ext.net.aliyun.vod;

import com.site0.walnut.ext.net.aliyun.JvmAliyunExecutor;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class cmd_aliyunvod extends JvmAliyunExecutor {

    @Override
    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        this._findHdlNameBy(sys, hc, "vod", WnAliyunVodConf.class);
    }

}
