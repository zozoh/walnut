package org.nutz.walnut.ext.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.nutz.lang.Strings;

public class WedisConfig {

    private String host;

    private int port;

    private boolean ssl;

    private String password;

    private int database;

    private int connectionTimeout;

    private int soTimeout;

    private int maxTotal;

    private int maxIdle;

    private int minIdle;

    public WedisConfig() {
        this("127.0.0.1", 6379, false, null, 2000, 5000);
    }

    public WedisConfig(String host,
                       int port,
                       boolean ssl,
                       String auth,
                       int connectionTimeout,
                       int soTimeout) {
        this.host = host;
        this.port = port;
        this.ssl = ssl;
        this.password = auth;
        this.connectionTimeout = connectionTimeout;
        this.soTimeout = soTimeout;
        this.maxTotal = 0;
        this.maxIdle = 0;
        this.minIdle = 0;
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

    public boolean hasPassword() {
        return !Strings.isBlank(password);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String auth) {
        this.password = auth;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
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

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public GenericObjectPoolConfig getPoolConfig() {
        GenericObjectPoolConfig pc = new GenericObjectPoolConfig();
        if (this.maxTotal > 0) {
            pc.setMaxTotal(maxTotal);
        }
        if (this.maxIdle > 0) {
            pc.setMaxIdle(maxIdle);
        }
        if (this.minIdle > 0) {
            pc.setMinIdle(minIdle);
        }
        return pc;
    }

    public String toKey() {
        return String.format("%s:%d[%d](%s/%s)",
                             host,
                             port,
                             this.database,
                             this.connectionTimeout,
                             this.soTimeout);
    }

    public String toString() {
        String s = this.toKey();
        if (ssl) {
            s += "(SSL)";
        }
        if (this.hasPassword()) {
            s += "***";
        }
        return s;
    }

}