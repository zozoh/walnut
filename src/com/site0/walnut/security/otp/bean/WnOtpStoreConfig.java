package com.site0.walnut.security.otp.bean;

/**
 * `~/.otp/{name}.json5` 的 store 段：存储器装配
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnOtpStoreConfig {

    /**
     * 存储器类型：redis → WnRedisOTPStore（目前唯一实现）
     */
    private String type;

    /**
     * 存储器的设置，随 type 决定具体类型（redis 时为 {@link WnOtpRedisStoreConfig}）
     */
    private WnOtpRedisStoreConfig config;

    public WnOtpStoreConfig() {
        this.type = "redis";
        this.config = new WnOtpRedisStoreConfig();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public WnOtpRedisStoreConfig getConfig() {
        return config;
    }

    public void setConfig(WnOtpRedisStoreConfig config) {
        this.config = config;
    }

}
