package com.site0.walnut.lookup.bean;

import com.site0.walnut.util.Wlang;

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
    /**
     * 这里可以声明一个数组，根据 partsSep，
     * <p>
     * 我们就从 hint 里可以拆分出多个具名变量，这样可以组织出更复杂的fetch语句
     * <p>
     * 它的值就是 ['name','age'] 这样的键名， 对应 hint 拆分后的每个部分， 如果不足，就用 'id_3' ...这样的变量来填充
     * <p>
     * 另外如果键名写成 'name:->${name}%' 则表示用 explain 来再次渲染值
     * 
     * @see com.site0.walnut.util.Wn#explainObj(org.nutz.lang.util.NutBean,
     *      Object)
     */
    private String[] fetchParts;

    /**
     * 获取前检查一下上下文，如果缺少指定变量，将返回空 默认的为 `['id']`
     */
    private String[] fetchRequireds;

    /**
     * 这里可以声明一个数组，根据 partsSep，
     * <p>
     * 我们就从 hint 里可以拆分出多个具名变量，这样可以组织出更复杂的query语句
     * <p>
     * 它的值就是 ['name','age'] 这样的键名， 对应 hint 拆分后的每个部分， 如果不足，就用 'hint_3'
     * ...这样的变量来填充
     * <p>
     * 另外如果键名写成 'name:->${name}%' 则表示用 explain 来再次渲染值
     * 
     * @see com.site0.walnut.util.Wn#explainObj(org.nutz.lang.util.NutBean,
     *      Object)
     */
    private String[] queryParts;

    /**
     * 获取前检查一下上下文，如果缺少指定变量，将返回空 默认的为 `['hint']`
     */
    private String[] queryRequireds;

    /**
     * 用来拆解 hint 的具名变量，如果不设置，则不会拆解<br>
     * 建议如果需要拆解具名变量时，采用`:` 作为分隔符
     */
    private String partsSep;

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

    public String[] getFetchParts() {
        return fetchParts;
    }

    public void setFetchParts(String[] fetchParts) {
        this.fetchParts = fetchParts;
    }

    public String[] getFetchRequireds() {
        if (null == fetchRequireds) {
            return Wlang.array("id");
        }
        return fetchRequireds;
    }

    public void setFetchRequireds(String[] fetchRequireds) {
        this.fetchRequireds = fetchRequireds;
    }

    public String[] getQueryParts() {
        return queryParts;
    }

    public void setQueryParts(String[] queryParts) {
        this.queryParts = queryParts;
    }

    public String[] getQueryRequireds() {
        if (null == queryRequireds) {
            return Wlang.array("hint");
        }
        return queryRequireds;
    }

    public void setQueryRequireds(String[] queryRequireds) {
        this.queryRequireds = queryRequireds;
    }

    public String getPartsSep() {
        return partsSep;
    }

    public void setPartsSep(String partsSep) {
        this.partsSep = partsSep;
    }

}
