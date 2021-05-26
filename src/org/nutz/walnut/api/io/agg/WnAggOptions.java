package org.nutz.walnut.api.io.agg;

import java.util.ArrayList;
import java.util.List;

import org.nutz.walnut.api.err.Er;

public class WnAggOptions {

    /**
     * 聚集分组键
     */
    private List<WnAggGroupKey> groupBy;

    /**
     * 聚集计算键名
     */
    private WnAggregateKey aggregateBy;

    /**
     * 聚集结果如何排序。 <code>null</code>表示不排序
     * 
     * @see org.nutz.walnut.api.io.agg.WnAggOrderBy
     */
    private List<WnAggOrderBy> orderBy;

    /**
     * 查找记录的最多限制。小于等于零表示全部数据
     */
    private int dataLimit;

    /**
     * 输出的统计数据最多限制。这个适合统计 Top10 之类的数据。小于等于零表示全部数据
     */
    private int outputLimit;

    public WnAggOptions() {
        this.groupBy = new ArrayList<>(5);
        this.orderBy = new ArrayList<>(5);
        this.dataLimit = 0;
        this.outputLimit = 0;
    }

    public void assertValid() {
        if (!this.hasGroupBy()) {
            throw Er.create("e.io.agg.NoGroupBy");
        }
        if (!this.hasAggregateBy()) {
            throw Er.create("e.io.agg.NoCalculateBy");
        }
    }

    public boolean isCOUNT() {
        return WnAggFunc.COUNT == aggregateBy.getFunc();
    }

    public boolean isMAX() {
        return WnAggFunc.MAX == aggregateBy.getFunc();
    }

    public boolean isMIN() {
        return WnAggFunc.MIN == aggregateBy.getFunc();
    }

    public boolean isSUM() {
        return WnAggFunc.SUM == aggregateBy.getFunc();
    }

    public boolean isAVG() {
        return WnAggFunc.AVG == aggregateBy.getFunc();
    }

    public boolean hasFuncName() {
        return null != aggregateBy.getFunc();
    }

    public WnAggFunc getFuncName() {
        return aggregateBy.getFunc();
    }

    public boolean hasGroupBy() {
        return null != groupBy && groupBy.size() > 0;
    }

    public List<WnAggGroupKey> getGroupBy() {
        return groupBy;
    }

    public void addGroupBy(WnAggGroupKey gk) {
        this.groupBy.add(gk);
    }

    public void setGroupBy(List<WnAggGroupKey> groupBy) {
        this.groupBy = groupBy;
    }

    public boolean hasAggregateBy() {
        return null != this.aggregateBy;
    }

    public WnAggregateKey getAggregateBy() {
        return aggregateBy;
    }

    public void setAggregateBy(WnAggregateKey aggeBy) {
        this.aggregateBy = aggeBy;
    }

    public boolean hasOrderBy() {
        return null != orderBy && orderBy.size() > 0;
    }

    public List<WnAggOrderBy> getOrderBy() {
        return orderBy;
    }

    public void addOrderBy(WnAggOrderBy ob) {
        this.orderBy.add(ob);
    }

    public void setOrderBy(List<WnAggOrderBy> orderBy) {
        this.orderBy = orderBy;
    }

    public boolean hasDataLimit() {
        return dataLimit > 0;
    }

    public int getDataLimit() {
        return dataLimit;
    }

    public void setDataLimit(int limit) {
        this.dataLimit = limit;
    }

    public boolean hasOutputLimit() {
        return this.outputLimit > 0;
    }

    public int getOutputLimit() {
        return outputLimit;
    }

    public void setOutputLimit(int outputLimit) {
        this.outputLimit = outputLimit;
    }

}
