package org.nutz.walnut.ext.net.sendmail.bean;

import org.nutz.lang.Strings;

public class WnMailConfig {

    /**
     * SMTP 服务地址
     */
    private String smtpHost;

    /**
     * SMTP 服务端口
     */
    private int smtpPort;

    /**
     * 发送者邮箱账户
     */
    private String account;

    /**
     * 发送账户别名（显示名称）
     */
    private String alias;

    /**
     * SMTP 服务的密码
     */
    private String password;

    /**
     * 是否启用 ssl 连接
     */
    private boolean ssl;

    /**
     * 发送时，默认采用的语言，默认 "zh-cn"
     */
    private String lang;

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getLang() {
        return Strings.sBlank(lang, "zh-cn");
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

}
