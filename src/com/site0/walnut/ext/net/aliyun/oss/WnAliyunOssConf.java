package com.site0.walnut.ext.net.aliyun.oss;

import com.site0.walnut.ext.net.aliyun.sdk.WnAliyunAccessKey;

public class WnAliyunOssConf extends WnAliyunAccessKey {

	/**
	 * 接入区域 ID，例如 "oss-cn-qingdao.aliyuncs.com"
	 */
	private String endpoint;

	/**
	 * 默认桶名称
	 */
	private String bucketName;

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

}
