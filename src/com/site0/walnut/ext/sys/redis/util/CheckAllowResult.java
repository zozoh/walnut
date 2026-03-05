package com.site0.walnut.ext.sys.redis.util;

import org.nutz.web.WebException;

import com.site0.walnut.api.err.Er;

public class CheckAllowResult {

    private boolean allow;

    private boolean allowDomain;

    private boolean allowDatabase;

    private String domain;

    private String myDB;

    private String allowDomains;

    private String allowDatabases;

    public CheckAllowResult(String domain) {
        this.allow = true;
        this.allowDomain = true;
        this.allowDatabase = true;
        this.domain = domain;
    }

    public CheckAllowResult notAllowDomains(String allowDomains) {
        this.allow = false;
        this.allowDomain = false;
        this.allowDomains = allowDomains;
        return this;
    }

    public CheckAllowResult notAllowDatabases(String myDB,
                                              String allowDatabases) {
        this.allow = false;
        this.allowDatabase = false;
        this.allowDatabases = allowDatabases;
        this.myDB = myDB;
        return this;
    }

    public boolean isAllow() {
        return allow;
    }

    public void setAllow(boolean isAllow) {
        this.allow = isAllow;
    }

    public boolean isAllowDomain() {
        return allowDomain;
    }

    public void setAllowDomain(boolean isAllowDomain) {
        this.allowDomain = isAllowDomain;
    }

    public boolean isAllowDatabase() {
        return allowDatabase;
    }

    public void setAllowDatabase(boolean isAllowDatabase) {
        this.allowDatabase = isAllowDatabase;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getMyDB() {
        return myDB;
    }

    public void setMyDB(String myDB) {
        this.myDB = myDB;
    }

    public String getAllowDomains() {
        return allowDomains;
    }

    public void setAllowDomains(String allowDomains) {
        this.allowDomains = allowDomains;
    }

    public String getAllowDatabases() {
        return allowDatabases;
    }

    public void setAllowDatabases(String allowDatabases) {
        this.allowDatabases = allowDatabases;
    }

    public WebException toException(String keyPrefix) {
        if (this.allow) {
            return null;
        }
        if (!this.allowDomain) {
            return Er.create(keyPrefix + ".DomainNoAllowed",
                             domain + "!=>" + allowDomains);
        }

        if (!this.allowDatabase) {
            return Er.create(keyPrefix + ".DatabaseNoAllowed",
                             myDB + "!=>" + allowDatabases);
        }
        return null;

    }

}
