package org.nutz.walnut.ext.net.aliyun.oss.hdl;

import org.nutz.walnut.ext.net.aliyun.oss.WnAliyunOssService;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class aliyunoss_lsdir extends aliyunoss_xxx {

	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
		WnAliyunOssService oss = getService(sys, hc);
		String objectName = hc.params.val_check(0);
		hc.output = oss.lsdir(objectName, null);
	}

}
