package org.nutz.walnut.ext.net.aliyun.vod;

import org.nutz.walnut.ext.net.aliyun.sdk.WnAliyunAccessKey;

public class WnAliyunVodConf extends WnAliyunAccessKey {

    /**
     * 接入区域 ID，例如 "cn-shanghai"
     */
    private String region;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

}
