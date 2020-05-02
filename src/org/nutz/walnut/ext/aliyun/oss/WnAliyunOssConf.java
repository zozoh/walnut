package org.nutz.walnut.ext.aliyun.oss;

import org.nutz.walnut.ext.aliyun.sdk.WnAliyunAccessKey;

public class WnAliyunOssConf extends WnAliyunAccessKey {

    /**
     * 接入区域 ID，例如 "oss-cn-qingdao.aliyuncs.com"
     */
    private String endpoint;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

}
