package org.nutz.walnut.ext.aliyun.oss.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.aliyun.oss.WnAliyunOssService;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

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
