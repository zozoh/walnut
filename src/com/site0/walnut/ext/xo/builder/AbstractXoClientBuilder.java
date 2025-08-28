package com.site0.walnut.ext.xo.builder;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.xo.util.XoClientWrapper;
import com.site0.walnut.ext.xo.util.XoClients;
import com.site0.walnut.util.Ws;

public abstract class AbstractXoClientBuilder<T> implements XoClientBuilder<T> {

    protected WnIo io;
    protected WnObj oHome;
    protected String name;

    protected XoClientWrapper<T> re;

    public AbstractXoClientBuilder(WnIo io, WnObj oHome, String name) {
        this.io = io;
        this.oHome = oHome;
        this.name = name;
        String key = XoClients.genClientKey(oHome, name);
        this.re = createClient(key);
    }

    protected abstract String getTokenPath();

    protected abstract String getConfigPath(String name);

    protected abstract XoClientWrapper<T> createClient(String clientKey);

    public abstract void loadConfig(NutMap props);

    protected String proxySchema;
    protected String proxyHost;
    protected int proxyPort;
    protected String proxyUsername;
    protected String proxyPassword;
    protected int proxyTimeoutConnect;
    protected int proxyTimeoutSocket;

    protected boolean hasProxy() {
        return !Ws.isBlank(proxyHost);
    }

    protected boolean isProxyNeedAuth() {
        return !Ws.isBlank(proxyUsername);
    }

    protected void loadProxyFromConfig(NutMap props) {
        this.proxySchema = props.getString("proxy-schema", "http");
        this.proxyHost = props.getString("proxy-host");
        this.proxyPort = props.getInt("proxy-port", 80);
        this.proxyUsername = props.getString("proxy-username");
        this.proxyPassword = props.getString("proxy-password");
        this.proxyTimeoutConnect = props.getInt("proxy-timeout-connect", 10);
        this.proxyTimeoutSocket = props.getInt("proxy-timeout-socket", 30);
    }

    public String getProxySchema() {
        return proxySchema;
    }

    public void setProxySchema(String proxySchema) {
        this.proxySchema = proxySchema;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

}