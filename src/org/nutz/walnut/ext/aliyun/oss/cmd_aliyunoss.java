package org.nutz.walnut.ext.aliyun.oss;

import org.nutz.walnut.ext.aliyun.JvmAliyunExecutor;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_aliyunoss extends JvmAliyunExecutor {

	protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
		OssClientPool pool = hc.ioc.get(OssClientPool.class);
		hc.setv("oss.pool", pool);
		_findHdlNameBy(sys, hc, "oss", WnAliyunOssConf.class);
	}
}
