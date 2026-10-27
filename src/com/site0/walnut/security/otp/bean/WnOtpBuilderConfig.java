package com.site0.walnut.security.otp.bean;

/**
 * `~/.otp/{name}.json5` 的 builder 段：生成器装配
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnOtpBuilderConfig {

    /**
     * 生成器类型：num → WnNumberOTPBuilder；char → WnCharOTPBuilder
     */
    private String type;

    /**
     * 生成器的设置（num / char 共用：位数与有效期）
     */
    private WnOtpNumBuilderConfig config;

    public WnOtpBuilderConfig() {
        this.type = "num";
        this.config = new WnOtpNumBuilderConfig();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public WnOtpNumBuilderConfig getConfig() {
        return config;
    }

    public void setConfig(WnOtpNumBuilderConfig config) {
        this.config = config;
    }

}
