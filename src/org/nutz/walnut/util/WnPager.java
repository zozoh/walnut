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
        this.skip = params.getInt("skip", 0);
        this.limit = params.getInt("limit", -1);
        this.countPage = params.is("pager");
        this.pgsz = limit > 0 ? limit : 50;
        this.pn = skip > 0 ? skip / pgsz + 1 : 1;
    }

    public WnPager clone() {
        WnPager wp = new WnPager();
        wp.skip = this.skip;
        wp.limit = this.limit;
        wp.countPage = this.countPage;
        wp.pgsz = this.pgsz;
        wp.pn = this.pn;
        wp.sum_count = this.sum_count;
        wp.sum_page = this.sum_page;
        return wp;
    }

}
