package org.nutz.walnut.ext.redis;

import org.nutz.lang.Strings;

public class WnRedisConfig {

    private String host;

    private int port;

    private boolean ssl;

    private String auth;

    private int select;

    public WnRedisConfig() {
        this("127.0.0.1", 6379, false, null, 0);
    }

    public WnRedisConfig(String host, int port, String auth) {
        this(host, port, false, auth, 0);
    }

    public WnRedisConfig(String host, int port, boolean ssl) {
        this(host, port, ssl, null, 0);
    }

    public WnRedisConfig(String host, int port, boolean ssl, String auth, int select) {
        this.host = host;
        this.port = port;
        this.ssl = ssl;
        this.auth = auth;
        this.select = select;
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

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public boolean hasAuth() {
        return !Strings.isBlank(auth);
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public int getSelect() {
        return select;
    }

    public void setSelect(int select) {
        this.select = select;
    }
    
    public String toKey() {
        return String.format("%s:%d", host, port);
    }

    public String toString() {
        String s = String.format("%s:%d->%d", host, port, select);
        if (ssl) {
            s += "(SSL)";
        }
        if (this.hasAuth()) {
            s += "***";
        }
        return s;
    }

}
