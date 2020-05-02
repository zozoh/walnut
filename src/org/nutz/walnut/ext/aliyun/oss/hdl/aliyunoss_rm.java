package org.nutz.walnut.ext.aliyun.oss.hdl;

import org.nutz.walnut.ext.aliyun.oss.WnAliyunOssService;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class aliyunoss_rm extends aliyunoss_xxx {

	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
		WnAliyunOssService oss = getService(sys, hc);
		for (String objectName : hc.params.vals) {
			oss.remove(objectName);
		}
	}

}
