package org.nutz.walnut.util;

import org.nutz.walnut.api.io.WnQuery;

public class WnPagerObj {

    private int defaultLimit;
    private int maxLimit;

    private int pageNumber;
    private int pageSize;
    private int pageCount;
    private long totalCount;
    private int count;

    public WnPagerObj() {
        this(100, 5000);
    }

    public WnPagerObj(int defaultLimit, int maxLimit) {
        this.defaultLimit = defaultLimit;
        this.maxLimit = maxLimit;
    }

    public WnPagerObj setBy(WnQuery q) {
        return set(q.limit(), q.skip());
    }

    public WnPagerObj set(int limit, int skip) {
        skip = Math.max(0, skip);
        this.pageSize = limit <= 0 ? defaultLimit
                                   : (maxLimit > 0 ? Math.min(maxLimit, limit) : limit);
        this.pageNumber = skip / this.pageSize + 1;
        return this;
    }

    public WnPagerObj setTotal(long tc) {
        this.totalCount = tc;
        this.pageCount = (int) Math.ceil(((double) tc) / ((double) this.pageSize));
        return this;
    }

    public void setupQuery(WnQuery q) {
        q.skip(this.getSkip());
        q.limit(this.getLimit());
    }

    @Override
    public WnPagerObj clone() {
        WnPagerObj wpo = new WnPagerObj();

        wpo.defaultLimit = this.defaultLimit;
        wpo.defaultLimit = this.defaultLimit;
        wpo.maxLimit = this.maxLimit;

        wpo.pageNumber = this.pageNumber;
        wpo.pageSize = this.pageSize;
        wpo.pageCount = this.pageCount;
        wpo.totalCount = this.totalCount;
        wpo.count = this.count;

        return wpo;
    }

    /*----------getter/setter---------------*/

    public int getDefaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(int defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    public int getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(int maxLimit) {
        this.maxLimit = maxLimit;
    }

    public int getLimit() {
        return this.pageSize;
    }

    public int getSkip() {
        return pageSize * (pageNumber - 1);
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    /*----------getter/setter---------------*/

}
