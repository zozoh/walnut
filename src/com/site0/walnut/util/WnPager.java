package com.site0.walnut.util;

import org.nutz.dao.pager.Pager;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.impl.box.WnSystem;

public class WnPager extends Pager {

    private static final long serialVersionUID = 1L;

    public int skip;
    public int limit;
    public boolean countPage;
    public int pgsz;
    public int pn;
    public long sum_count;
    public int sum_page;
    public static int DEAULT_LIMIT = 500;

    public WnPager() {
        this.sum_count = -1;
        this.sum_page = -1;
    }

    public WnPager(Pager pg) {
        this(pg.getPageSize(), pg.getOffset());
        this.countPage = true;
        this.setSumCount(pg.getRecordCount());
    }

    public WnPager(int limit, int skip) {
        this();
        this.set(limit, skip);
    }

    public WnPager(ZParams params) {
        this();
        this.skip = params.getInt("skip", 0);
        this.limit = params.getInt("limit", DEAULT_LIMIT);
        // boolean breakLimit = params.is("blimit", false);

        // 是否计算分页
        this.countPage = params.is("pager") && this.limit > 0;

        // 最大不能超过一千条
        if (this.limit < 0) {
            this.limit = DEAULT_LIMIT;
        }
        // 查询的上限是 10W 条
        if (this.limit > 100000) {
            this.limit = 100000;
        }
        this.pgsz = limit > 0 ? limit : DEAULT_LIMIT;
        this.pn = skip > 0 ? skip / pgsz + 1 : 1;
    }

    public WnPager set(int limit, int skip) {
        this.countPage = true;
        this.skip = skip;
        this.limit = limit;
        this.pgsz = limit > 0 ? limit : DEAULT_LIMIT;
        this.pn = skip > 0 ? skip / pgsz + 1 : 1;
        return this;
    }

    public void setupQuery(WnSystem sys, WnQuery q) {
        setupQuery(sys.io, q);
    }

    public void setupQuery(WnIo io, WnQuery q) {
        // 看看是否需要查询分页信息
        if (this.countPage && this.limit > 0) {
            // this.sum_count = (int) io.count(q);
            // this.sum_page = (int) Math.ceil(((double) this.sum_count) /
            // ((double) this.limit));
            WnQuery q2 = q.clone(); // 为了防止 Io 篡改 query 的内容...
            long sum = io.count(q2);
            this.setSumCount(sum);
        }

        if (this.skip > 0)
            q.skip(this.skip);

        if (this.limit > 0)
            q.limit(this.limit);
    }

    public void setSumCount(long sc) {
        this.sum_count = sc;
        if (this.countPage && this.limit > 0) {
            this.sum_page = (int) Math.ceil(((double) this.sum_count) / ((double) this.limit));
            this.setRecordCount((int) sc);
        }
    }

    public WnPagerObj toPagerObj(int count) {
        WnPagerObj wpo = new WnPagerObj(DEAULT_LIMIT, 5000);
        wpo.set(limit, skip);
        wpo.setTotal(sum_count);
        wpo.setCount(count);
        return wpo;
    }

    @Override
    public WnPager clone() {
        WnPager wp = new WnPager();
        wp.skip = this.skip;
        wp.limit = this.limit;
        wp.countPage = this.countPage;
        wp.pgsz = this.pgsz;
        wp.pn = this.pn;
        wp.sum_count = this.sum_count;
        wp.sum_page = this.sum_page;
        wp.setRecordCount((int) this.sum_count);
        return wp;
    }

    public int getOffset() {
        return skip;
    }

    public int getPageSize() {
        return limit;
    }
}
