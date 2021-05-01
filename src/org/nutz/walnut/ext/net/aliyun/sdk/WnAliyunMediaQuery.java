package org.nutz.walnut.ext.net.aliyun.sdk;

public class WnAliyunMediaQuery {

    private String feilds;

    private String match;

    private int pageNo;

    private int pageSize;

    private String searchType;

    private String sortBy;

    private String scrollToken;

    public WnAliyunMediaQuery() {
        this.pageNo = 1;
        this.pageSize = 20;
    }

    public String getFeilds() {
        return feilds;
    }

    public void setFeilds(String feilds) {
        this.feilds = feilds;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getScrollToken() {
        return scrollToken;
    }

    public void setScrollToken(String scrollToken) {
        this.scrollToken = scrollToken;
    }

}
