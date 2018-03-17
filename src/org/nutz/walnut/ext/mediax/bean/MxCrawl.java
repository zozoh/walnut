package org.nutz.walnut.ext.mediax.bean;

import java.util.Date;

public class MxCrawl {

    public MxCrawl() {
        this.limit = 0;
    }

    public String uri;

    public Date lastDate;

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
