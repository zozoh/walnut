package com.site0.walnut.ext.data.entity.history.fake;

import org.nutz.lang.util.NutMap;

public class HistoryFakeSchema {

    private String path;

    private NutMap query;

    private NutMap sort;

    private int limit;

    private int skip;

    public HistoryFakeSchema() {
        this.limit = 20;
        this.skip = 0;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean hasQuery() {
        return null != query && query.size() > 0;
    }

    public NutMap getQuery() {
        return query;
    }

    public void setQuery(NutMap query) {
        this.query = query;
    }

    public boolean hasSort() {
        return null != this.sort && this.sort.size() > 0;
    }

    public NutMap getSort() {
        return sort;
    }

    public void setSort(NutMap sort) {
        this.sort = sort;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

}
