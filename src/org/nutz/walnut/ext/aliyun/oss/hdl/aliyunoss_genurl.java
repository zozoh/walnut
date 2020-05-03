package org.nutz.walnut.ext.aliyun.oss.hdl;

import java.net.URL;

import org.nutz.walnut.ext.aliyun.oss.WnAliyunOssService;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class aliyunoss_genurl extends aliyunoss_xxx {

	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
		WnAliyunOssService oss = getService(sys, hc);
		URL url = oss.genURL(hc.params.val_check(0), "GET", hc.params.getInt("t", 600*1000));
		sys.out.print(url.toString());
	}

}
