package com.site0.walnut.security.otp.bean;

/**
 * 数字/字符 OTP 生成器的设置：位数与有效期
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnOtpNumBuilderConfig {

    /**
     * 码的位数
     */
    private int n;

    /**
     * 码的有效期（秒），同时也是存储时的 TTL
     */
    private int duration;

    public WnOtpNumBuilderConfig() {
        this.n = 6;
        this.duration = 300;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

}
