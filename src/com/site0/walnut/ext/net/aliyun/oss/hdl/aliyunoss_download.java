package com.site0.walnut.ext.net.aliyun.oss.hdl;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.net.aliyun.oss.WnAliyunOssService;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class aliyunoss_download extends aliyunoss_xxx {

	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
		WnAliyunOssService oss = getService(sys, hc);
		String objectName = hc.params.val_check(0);
		String path = hc.params.val_check(1);
		path = Wn.normalizeFullPath(path, sys);
		WnObj tmp = sys.io.createIfNoExists(null, path, WnRace.FILE);
		oss.downlaod(tmp, objectName);
	}

}
