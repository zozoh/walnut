package org.nutz.walnut.ext.entity.newsfeed;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

public class FeedConfig {

    private String feedName;

    private String jdbcUrl;

    private String jdbcUserName;

    private String jdbcPassword;

    private String accounts;

    private String tableName;

    private NutMap extFieldsMapping;

    public boolean hasFeedName() {
        return !Strings.isBlank(this.feedName);
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getJdbcUserName() {
        return jdbcUserName;
    }

    public void setJdbcUserName(String jdbcUserName) {
        this.jdbcUserName = jdbcUserName;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    public void setJdbcPassword(String jdbcPassword) {
        this.jdbcPassword = jdbcPassword;
    }

    public String getAccounts() {
        return accounts;
    }

    public void setAccounts(String accounts) {
        this.accounts = accounts;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public NutMap getExtFieldsMapping() {
        return extFieldsMapping;
    }

    public void setExtFieldsMapping(NutMap extFieldsMapping) {
        this.extFieldsMapping = extFieldsMapping;
    }

}
