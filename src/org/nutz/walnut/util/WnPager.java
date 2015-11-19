package org.nutz.walnut.util;

public class WnPager {

    public int skip;
    public int limit;
    public boolean countPage;
    public int pgsz;
    public int pn;
    public int sum_count;
    public int sum_page;

    public WnPager() {
        this.sum_count = -1;
        this.sum_page = -1;
    }

    public WnPager(ZParams params) {
        this();
        this.skip = params.getInt("skip", -1);
        this.limit = params.getInt("limit", -1);
        this.countPage = params.is("pager");
        this.pgsz = limit > 0 ? limit : 50;
        this.pn = skip > 0 ? skip / pgsz + 1 : 1;
    }

}
