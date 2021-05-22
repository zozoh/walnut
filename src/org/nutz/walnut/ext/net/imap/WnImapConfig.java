package org.nutz.walnut.ext.net.imap;

import java.util.HashMap;
import java.util.Map;

import org.nutz.walnut.WnVersion;

/**
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnImapConfig {

    /**
     * 服务器地址，譬如: <code>imap.163.com</code>
     */
    private String host;

    /**
     * 端口，通常为: <code>143</code>，如果是 SSL，通常是<code>993</code>
     */
    private int port;

    /**
     * 邮箱账户，譬如 <code>zozohtnt@163.com</code>
     */
    private String account;

    /**
     * 密码，譬如 <code>SV...JD</code>
     * <p>
     * 通常是邮箱服务提供商给的访问凭证或者密钥
     */
    private String passwd;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 客户端版本
     */
    private String clientVersion;

    public WnImapConfig() {
        this.port = 143;
        this.clientName = "Walnut-Imap-Client";
        this.clientVersion = WnVersion.get();
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

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public Map<String, String> getClientParams() {
        Map<String, String> params = new HashMap<>();
        params.put("name", clientName);
        params.put("version", clientVersion);
        return params;
    }
}
