package com.site0.walnut.security.otp.bean;

import com.site0.walnut.util.Ws;

public class WnOTPBean {

    public WnOTPBean() {
        this.duration = 300;
    }

    public WnOTPBean(int duration) {
        this.duration = duration;
    }

    public WnOTPBean(int duration, WnOTPType type, String account, String val) {
        this(duration);
        this.type = type;
        this.account = account;
        this.value = val;
    }

    public static String getKey(WnOTPType type, String account) {
        if (null == type || Ws.isBlank(account)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(account);
        sb.append(':');
        sb.append(type.toString());
        return sb.toString();
    }

    public String toKey() {
        return getKey(type, account);
    }

    @Override
    public String toString() {
        return String.format("%s:%s [%s](du=%s, retry=%s)",
                             type,
                             account,
                             value,
                             duration,
                             retryTimes);
    }

    @Override
    public boolean equals(Object input) {
        if (null == input)
            return false;
        if (input instanceof WnOTPBean) {
            WnOTPBean otp = (WnOTPBean) input;
            return otp.isSameType(this.type)
                   && otp.isSameAccount(this.account)
                   && otp.isSameValue(this.value);
        }
        return false;
    }

    /**
     * 动态密码类型
     */
    private WnOTPType type;

    /**
     * 动态密码账号，或者是邮箱，或者是手机号
     */
    private String account;

    /**
     * 动态密码的值
     */
    private String value;

    /**
     * 指定密码的有效时长（秒）
     */
    private int duration;

    /**
     * 校验重试次数
     */
    private int retryTimes;

    /**
     * 发送的时间: UTC: <code>yyyy-MM-dd HH:mm:ss</code>
     */
    private String sendAt;

    public WnOTPType getType() {
        return type;
    }

    public void setType(WnOTPType type) {
        this.type = type;
    }

    public boolean isSms() {
        return WnOTPType.SMS == this.type;
    }

    public boolean isEmail() {
        return WnOTPType.EMAIL == this.type;
    }

    public boolean isSameType(WnOTPType type) {
        return null != this.type && null != type && this.type.equals(type);
    }

    public boolean hasAccount() {
        return !Ws.isBlank(account);
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public boolean isSameAccount(String account) {
        return null != this.account
               && null != account
               && this.account.equals(account);
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean hasValue() {
        return !Ws.isBlank(value);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSameValue(String val) {
        return null != this.value && null != val && this.value.equals(val);
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTime) {
        this.retryTimes = retryTime;
    }

    public void increaseRetryTimes() {
        this.retryTimes++;
    }

    public String getSendAt() {
        return sendAt;
    }

    public void setSendAt(String sendAt) {
        this.sendAt = sendAt;
    }

}
