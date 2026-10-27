package com.site0.walnut.security.otp.bean;

/**
 * `~/.otp/{name}.json5` 的 sender 段：发送器装配
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnOtpSenderConfig {

    /**
     * 发送器类型：EMAIL → WnSysEmailOTPSender（目前唯一实现）
     */
    private String type;

    /**
     * 发送器的设置，随 type 决定具体类型（EMAIL 时为 {@link WnOtpEmailSenderConfig}）
     */
    private WnOtpEmailSenderConfig config;

    public WnOtpSenderConfig() {
        this.type = "EMAIL";
        this.config = new WnOtpEmailSenderConfig();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public WnOtpEmailSenderConfig getConfig() {
        return config;
    }

    public void setConfig(WnOtpEmailSenderConfig config) {
        this.config = config;
    }

}
