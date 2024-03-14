package com.site0.walnut.ext.net.aliyun.oss.hdl;

import com.site0.walnut.ext.net.aliyun.oss.WnAliyunOssService;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class aliyunoss_lsdir extends aliyunoss_xxx {

	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
		WnAliyunOssService oss = getService(sys, hc);
		String objectName = hc.params.val_check(0);
		hc.output = oss.lsdir(objectName, null);
	}

}
