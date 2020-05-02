package org.nutz.walnut.ext.aliyun.sdk;

public class WnAliyunMessageCallback {

    private String url;

    private String type;

    public WnAliyunMessageCallback(String url) {
        this.url = url;
        int pos = url.indexOf(':');
        this.type = url.substring(0, pos);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
