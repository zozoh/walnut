package com.site0.walnut.ext.net.aliyun.oss;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.nutz.ioc.loader.annotation.IocBean;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

@IocBean(depose = "depose")
public class OssClientPool {

	public Map<String, OSS> clients = new HashMap<String, OSS>();

	public void depose() {
		clients = Collections.unmodifiableMap(clients);
		for (OSS client : clients.values()) {
			client.shutdown();
		}
	}

	public OSS get(String endpoint, String accessKeyId, String accessKeySecret) {
		String md5 = Wlang.sha1(endpoint + " " + accessKeyId + " " + accessKeySecret);
		return clients.computeIfAbsent(md5, (key) -> {
			return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
		});
	}

	public OSS get(NutMap conf) {
		String endpoint = conf.getString("endpoint");
		String accessKeyId = conf.getString("accessKeyId");
		String accessKeySecret = conf.getString("accessKeySecret");
		return get(endpoint, accessKeyId, accessKeySecret);
	}

	public OSS get(WnAliyunOssConf conf) {
		return get(conf.getEndpoint(), conf.getId(), conf.getSecret());
	}
}
