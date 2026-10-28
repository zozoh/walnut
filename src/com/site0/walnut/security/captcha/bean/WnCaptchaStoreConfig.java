package com.site0.walnut.security.captcha.bean;

import com.site0.walnut.util.Ws;

/**
 * captcha 答案与票据的存储配置。
 *
 * <p>
 * 当前仅支持 Redis（与 OTP 的存储选择一致），连接配置名引用
 * <code>~/.redis/{name}.json</code>。
 */
public class WnCaptchaStoreConfig {

    /**
     * 存储类型，当前仅支持 <code>redis</code>
     */
    private String type = "redis";

    /**
     * Redis 连接配置名（<code>~/.redis/{name}.json</code>）
     */
    private String name = "default";

    /**
     * 键前缀，留空则缺省为 <code>captcha:{服务配置名}:</code>
     */
    private String prefix;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
        return !Ws.isBlank(prefix);
    }

}
