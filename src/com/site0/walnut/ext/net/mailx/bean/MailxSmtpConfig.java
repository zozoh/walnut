package com.site0.walnut.ext.net.mailx.bean;

import com.site0.walnut.util.Ws;
import org.simplejavamail.api.mailer.config.TransportStrategy;

public class MailxSmtpConfig {
    /**
     * SMTP 服务地址
     */
    private String host;

    /**
     * 发件人邮件地址，如果不设置，应该默认采用 account
     */
    private String from;

    /**
     * SMTP 服务端口
     */
    private int port;

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
     * 默认模板采用的语言
     */
    private String lang;

    /**
     * SMTP 发送策略
     */
    private TransportStrategy strategy;

    /**
     * 发信时，默认固定要添加的 mail_to 如果不指定，则无视
     */
    private String[] mailTo;

    /**
     * 发信时，默认固定要添加的 mail_cc 如果不指定，则无视
     */
    private String[] mailCc;

    /**
     * 发信时，默认固定要添加的 mail_bcc 如果不指定，则无视
     */
    private String[] mailBcc;

    public String getHost() {
        return host;
    }

    public void setHost(String smtpHost) {
        this.host = smtpHost;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int smtpPort) {
        this.port = smtpPort;
    }

    public String getMailFrom() {
        String str = Ws.sBlanks(from, account);
        return str;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
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

    public boolean hasAlias() {
        return !Ws.isBlank(alias);
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

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public TransportStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(TransportStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean hasMailTo() {
        return null != mailTo && mailTo.length > 0;
    }

    public String[] getMailTo() {
        return mailTo;
    }

    public void setMailTo(String[] mailTo) {
        this.mailTo = mailTo;
    }

    public boolean hasMailCc() {
        return null != mailCc && mailCc.length > 0;
    }

    public String[] getMailCc() {
        return mailCc;
    }

    public void setMailCc(String[] mailCc) {
        this.mailCc = mailCc;
    }

    public boolean hasMailBcc() {
        return null != mailBcc && mailBcc.length > 0;
    }

    public String[] getMailBcc() {
        return mailBcc;
    }

    public void setMailBcc(String[] mailBcc) {
        this.mailBcc = mailBcc;
    }

}
