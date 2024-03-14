package com.site0.walnut.ext.media.mediax.bean;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.nutz.lang.Times;

public class MxCrawl {

    public MxCrawl() {
        this.limit = 0;
    }

    /**
     * 指定 URI
     */
    public URI uri;

    /**
     * 指定一个日期，仅爬取这个日期之后的数据
     */
    public Date lastDate;

    /**
     * 限制最多爬取数量
     */
    public int limit;

    public MxCrawl uri(String uri) throws URISyntaxException {
        return uri(new URI(uri));
    }

    public MxCrawl uri(URI uri) {
        this.uri = uri;
        return this;
    }

    public MxCrawl lastDate(String last) {
        this.lastDate = Times.D(last);
        return this;
    }

    public MxCrawl lastDate(long ms) {
        this.lastDate = Times.D(ms);
        return this;
    }

    public MxCrawl limit(int limit) {
        this.limit = limit;
        return this;
    }

}
