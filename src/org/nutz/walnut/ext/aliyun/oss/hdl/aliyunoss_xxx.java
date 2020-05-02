package org.nutz.walnut.ext.aliyun.oss.hdl;

import org.nutz.walnut.ext.aliyun.oss.OssClientPool;
import org.nutz.walnut.ext.aliyun.oss.WnAliyunOssConf;
import org.nutz.walnut.ext.aliyun.oss.WnAliyunOssService;
import org.nutz.walnut.ext.aliyun.sdk.WnAliyuns;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public abstract class aliyunoss_xxx implements JvmHdl {

	public WnAliyunOssService getService(WnSystem sys, JvmHdlContext hc) {
		OssClientPool pool = (OssClientPool) hc.get("oss.pool");
		WnAliyunOssConf conf = WnAliyuns.getConf(hc, WnAliyunOssConf.class);
		return new WnAliyunOssService(sys.io, pool.get(conf), conf.getBucketName());
	}
}
