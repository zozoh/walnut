package org.nutz.dao.pager;

import java.io.Serializable;

import org.nutz.lang.util.PageInfo;
import org.nutz.log.Log;
import org.nutz.log.Logs;

public class Pager implements PageInfo, Serializable {

    private static final long serialVersionUID = 8848523495013555357L;

    private static final Log log = Logs.get();

    /**
     * 改变这个，当每页大小超过 MAX_FETCH_SIZE 时，这个将是默认的 fetchSize
     */
    public static int DEFAULT_PAGE_SIZE = 20;

    /**
     * ResultSet 最大的 fetch size
     */
    public static int MAX_FETCH_SIZE = 200;

    // private static final int FIRST_PAGE_NUMBER = 1;

    private int pageNumber;
    private int pageSize;
    private int pageCount;
    private int recordCount;

    public Pager() {
        this.pageNumber = 1;
        this.pageSize = DEFAULT_PAGE_SIZE;
    }

    public Pager(int pageNumber) {
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        this.pageNumber = pageNumber;
        this.pageSize = DEFAULT_PAGE_SIZE;
    }

    public Pager(int pageNumber, int pageSize) {
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        if (pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public Pager resetPageCount() {
        pageCount = -1;
        return this;
    }

    @Override
    public int getPageCount() {
        if (pageCount < 0) {
            pageCount = (int) Math.ceil((double) recordCount / pageSize);
        }
        return pageCount;
    }

    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public int getRecordCount() {
        return recordCount;
    }

    @Override
    public Pager setPageNumber(int pn) {
        if (1 > pn && log.isInfoEnabled()) {
            log.infof("PageNumber shall start at 1, but input is %d, that mean pager is disable", pn);
        }
        pageNumber = pn;
        return this;
    }

    @Override
    public Pager setPageSize(int pageSize) {
        this.pageSize = (pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE);
        return resetPageCount();
    }

    @Override
    public Pager setRecordCount(int recordCount) {
        this.recordCount = recordCount > 0 ? recordCount : 0;
        this.pageCount = (int) Math.ceil((double) recordCount / pageSize);
        return this;
    }

    @Override
    public int getOffset() {
        return pageSize * (pageNumber - 1);
    }

    @Override
    public String toString() {
        return String.format(    "size: %d, total: %d, page: %d/%d",
                                pageSize,
                                recordCount,
                                pageNumber,
                                this.getPageCount());
    }

    @Override
    public boolean isFirst() {
        return pageNumber == 1;
    }

    @Override
    public boolean isLast() {
        if (pageCount == 0) {
            return true;
        }
        return pageNumber == pageCount;
    }

    @Override
    public boolean hasNext() {
        return !isLast();
    }

    @Override
    public boolean hasPrevious() {
        return !isFirst();
    }
}
