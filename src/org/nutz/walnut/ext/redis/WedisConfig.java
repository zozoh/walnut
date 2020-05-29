package org.nutz.walnut.ext.redis;

import org.nutz.lang.Strings;

public class WedisConfig {

    private String host;

    private int port;

    private boolean ssl;

    private String auth;

    private int connectionTimeout;

    private int soTimeout;

    private int select;

    public WedisConfig() {
        this("127.0.0.1", 6379, false, null, 2000, 5000, 0);
    }

    public WedisConfig(String host,
                         int port,
                         boolean ssl,
                         String auth,
                         int connectionTimeout,
                         int soTimeout,
                         int select) {
        this.host = host;
        this.port = port;
        this.ssl = ssl;
        this.auth = auth;
        this.connectionTimeout = connectionTimeout;
        this.soTimeout = soTimeout;
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

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public int getSelect() {
        return select;
    }

    public void setSelect(int select) {
        this.select = select;
    }

    public String toKey() {
        return String.format("%s:%d[%d]TO%s/%s",
                             host,
                             port,
                             select,
                             this.connectionTimeout,
                             this.soTimeout);
    }

    public String toString() {
        String s = this.toKey();
        if (ssl) {
            s += "(SSL)";
        }
        if (this.hasAuth()) {
            s += "***";
        }
        return s;
    }

}
