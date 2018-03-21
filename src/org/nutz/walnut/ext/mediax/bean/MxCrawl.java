package org.nutz.walnut.ext.mediax.bean;

import java.util.Date;

public class MxCrawl {

    public MxCrawl() {
        this.limit = 0;
    }

    /**
     * 指定 URI
     */
    public String uri;

    /**
     * 指定一个日期，仅爬取这个日期之后的数据
     */
    public Date lastDate;

    /**
     * 限制最多爬取数量
     */
    public int limit;

    public MxCrawl uri(String uri) {
        this.uri = uri;
        return this;
    }

    public MxCrawl lastDate(Date last) {
        this.lastDate = last;
        return this;
    }

    public MxCrawl limit(int limit) {
        this.limit = limit;
        return this;
    }

}
