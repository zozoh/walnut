package com.site0.walnut.ext.net.aliyun.oss.hdl;

import com.site0.walnut.ext.net.aliyun.oss.OssClientPool;
import com.site0.walnut.ext.net.aliyun.oss.WnAliyunOssConf;
import com.site0.walnut.ext.net.aliyun.oss.WnAliyunOssService;
import com.site0.walnut.ext.net.aliyun.sdk.WnAliyuns;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public abstract class aliyunoss_xxx implements JvmHdl {

	public WnAliyunOssService getService(WnSystem sys, JvmHdlContext hc) {
		OssClientPool pool = (OssClientPool) hc.get("oss.pool");
		WnAliyunOssConf conf = WnAliyuns.getConf(hc, WnAliyunOssConf.class);
		return new WnAliyunOssService(sys.io, pool.get(conf), conf.getBucketName());
	}
}
