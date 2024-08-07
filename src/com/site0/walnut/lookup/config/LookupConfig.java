package com.site0.walnut.lookup.config;

public class LookupConfig {

    private LookupType type;

    /**
     * 如果类型为 SQL，则需要一个数据库连接方式
     */
    private String daoPath;

    /**
     * 如果类型为 SQL， 还需要一个 SQL 语句作为查询模板
     */
    private String sqlQuery;

    /**
     * 如果类型为 SQL， 还需要一个 SQL 语句作为单条数据获取模板
     */
    private String sqlFetch;

    public LookupType getType() {
        return type;
    }

    public void setType(LookupType type) {
        this.type = type;
    }

    public String getDaoPath() {
        return daoPath;
    }

    public void setDaoPath(String daoPath) {
        this.daoPath = daoPath;
    }

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public String getSqlFetch() {
        return sqlFetch;
    }

    public void setSqlFetch(String sqlFetch) {
        this.sqlFetch = sqlFetch;
    }

}
