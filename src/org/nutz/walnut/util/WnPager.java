package org.nutz.walnut.util;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.box.WnSystem;

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

    public void setupQuery(WnSystem sys, WnQuery q) {
        setupQuery(sys.io, q);
    }

    public void setupQuery(WnIo io, WnQuery q) {
        // 看看是否需要查询分页信息
        if (this.countPage && this.limit > 0) {
            this.sum_count = (int) io.count(q);
            this.sum_page = (int) Math.ceil(((double) this.sum_count) / ((double) this.limit));
        }

        if (this.skip > 0)
            q.skip(this.skip);

        if (this.limit > 0)
            q.limit(this.limit);
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
