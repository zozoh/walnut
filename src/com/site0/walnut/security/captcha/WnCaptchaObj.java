package com.site0.walnut.security.captcha;

/**
 * 一份验证码的载体：服务器生成后 {@link #toString()} 输出给客户端，
 * 客户端据此绘制验证码界面。
 */
public interface WnCaptchaObj {

    /**
     * @return 本次验证码的唯一标识
     */
    String getId();

    /**
     * @return 输出给客户端的 JSON 字符串（结构由具体实现定义）
     */
    String toString();

    /**
     * 从字符串还原（与 {@link #toString()} 互逆）
     *
     * @param input
     *            JSON 字符串
     */
    void fromString(String input);

}
