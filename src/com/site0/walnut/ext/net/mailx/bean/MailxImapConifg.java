package com.site0.walnut.ext.net.mailx.bean;

public class MailxImapConifg {
    /**
     * IMAP 服务地址
     */
    private String host;

    /**
     * IMAP 服务端口. -1 表示采用默认端口
     */
    private int port;

    /**
     * 邮箱账户
     */
    private String account;

    /**
     * 邮箱密码
     */
    private String password;

    /*
     * 值如果没有指定 authProvider， 则采用一个默认的认证实现 除了 office365 以外更多的 Provider 实现正在赶来的路上
     */
    private MailxImapProviderConfig provider;

    private String inboxName;

    public MailxImapConifg() {
        this.host = null;
        this.port = -1;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean hasProvider() {
        return null != provider;
    }

    public MailxImapProviderConfig getProvider() {
        return provider;
    }

    public void setProvider(MailxImapProviderConfig provider) {
        this.provider = provider;
    }

    public String getInboxName() {
        return inboxName;
    }

    public void setInboxName(String inboxName) {
        this.inboxName = inboxName;
    }

}
