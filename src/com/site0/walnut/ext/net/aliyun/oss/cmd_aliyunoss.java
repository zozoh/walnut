package com.site0.walnut.ext.net.aliyun.oss;

import com.site0.walnut.ext.net.aliyun.JvmAliyunExecutor;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class cmd_aliyunoss extends JvmAliyunExecutor {

	protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
		OssClientPool pool = hc.ioc.get(OssClientPool.class);
		hc.setv("oss.pool", pool);
		_findHdlNameBy(sys, hc, "oss", WnAliyunOssConf.class);
	}
}
