package com.site0.walnut.security.otp.bean;

/**
 * Redis OTP 存储器的设置
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnOtpRedisStoreConfig {

    /**
     * Redis 连接配置名：<code>~/.redis/{name}.json</code>
     */
    private String name;

    /**
     * 存储键前缀，不设置则默认 <code>otp:{服务配置名}:</code>——
     * 前缀是在同一个 Redis 里区分场景的机制
     */
    private String prefix;

    /**
     * 校验重试上限，超过即删码作废
     */
    private int maxRetryTimes;

    public WnOtpRedisStoreConfig() {
        this.name = "default";
        this.maxRetryTimes = 3;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean hasPrefix() {
        return null != prefix && !prefix.isEmpty();
    }

    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

}
