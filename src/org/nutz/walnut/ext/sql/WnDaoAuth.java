package org.nutz.walnut.ext.sql;

import org.nutz.lang.Strings;

public class WnDaoAuth {

    private String url;

    private String username;

    private String password;

    private int maxActive;

    private int maxWait;

    private boolean testWhileIdle;

    public WnDaoAuth() {
        this.maxActive = 50;
        this.maxWait = 15000;
        this.testWhileIdle = true;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean hasPassword() {
        return !Strings.isBlank(password);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public String toKey() {
        return String.format("%s%s@[%s](%d/%d)",
                             testWhileIdle ? "=" : ":",
                             username,
                             url,
                             maxActive,
                             maxWait);
    }

    public String toString() {
        String s = this.toKey();
        if (this.hasPassword()) {
            s += "***";
        }
        return s;
    }

}
