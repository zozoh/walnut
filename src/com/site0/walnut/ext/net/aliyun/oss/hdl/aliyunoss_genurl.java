package com.site0.walnut.ext.net.aliyun.oss.hdl;

import java.net.URL;

import com.site0.walnut.ext.net.aliyun.oss.WnAliyunOssService;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class aliyunoss_genurl extends aliyunoss_xxx {

	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
		WnAliyunOssService oss = getService(sys, hc);
		URL url = oss.genURL(hc.params.val_check(0), "GET", hc.params.getInt("t", 600*1000));
		sys.out.print(url.toString());
	}

}
