package org.nutz.walnut.ext.aliyun.oss.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.aliyun.oss.WnAliyunOssService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class aliyunoss_upload implements JvmHdl {

	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
		WnObj conf_obj = (WnObj) hc.get("conf_obj");
		WnAliyunOssService oss = new WnAliyunOssService(sys.io, conf_obj);
		String objectName = hc.params.val_check(0);
		String path = hc.params.val_check(1);
		path = Wn.normalizeFullPath(path, sys);
		WnObj tmp = sys.io.check(null, path);
		oss.upload(tmp, objectName);
	}

}
