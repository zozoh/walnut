package com.site0.walnut.security.otp.bean;

/**
 * `~/.otp/{name}.json5` 的服务配置：sender / builder / store 三段角色装配
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnOtpConfig {

    /**
     * 发送器装配
     */
    private WnOtpSenderConfig sender;

    /**
     * 生成器装配
     */
    private WnOtpBuilderConfig builder;

    /**
     * 存储器装配
     */
    private WnOtpStoreConfig store;

    public WnOtpConfig() {
        this.sender = new WnOtpSenderConfig();
        this.builder = new WnOtpBuilderConfig();
        this.store = new WnOtpStoreConfig();
    }

    public WnOtpSenderConfig getSender() {
        return sender;
    }

    public void setSender(WnOtpSenderConfig sender) {
        this.sender = sender;
    }

    public WnOtpBuilderConfig getBuilder() {
        return builder;
    }

    public void setBuilder(WnOtpBuilderConfig builder) {
        this.builder = builder;
    }

    public WnOtpStoreConfig getStore() {
        return store;
    }

    public void setStore(WnOtpStoreConfig store) {
        this.store = store;
    }

}
