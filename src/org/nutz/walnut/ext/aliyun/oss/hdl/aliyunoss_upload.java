package org.nutz.walnut.ext.aliyun.oss.hdl;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.aliyun.oss.WnAliyunOssService;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class aliyunoss_upload extends aliyunoss_xxx {

	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
		WnAliyunOssService oss = getService(sys, hc);
		String objectName = hc.params.val_check(0);
		String path = hc.params.val_check(1);
		path = Wn.normalizeFullPath(path, sys);
		WnObj tmp = sys.io.check(null, path);
		if (tmp.isDIR()) {
			return; // 文件夹就无视
		}
		NutMap meta = new NutMap();
		if (hc.params.has("meta")) {
			meta = Lang.map(hc.params.get("meta"));
		}
		oss.upload(tmp, objectName, meta, hc.params.is("force", false));
	}

}
