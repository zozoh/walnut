package com.site0.walnut.ext.net.aliyun.oss.hdl;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.aliyun.oss.WnAliyunOssService;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

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
			meta = Wlang.map(hc.params.get("meta"));
		}
		oss.upload(tmp, objectName, meta, hc.params.is("force", false));
	}

}
