package com.site0.walnut.ext.net.aliyun.oss.hdl;

import com.site0.walnut.ext.net.aliyun.oss.WnAliyunOssService;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class aliyunoss_rm extends aliyunoss_xxx {

	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
		WnAliyunOssService oss = getService(sys, hc);
		for (String objectName : hc.params.vals) {
			oss.remove(objectName);
		}
	}

}
