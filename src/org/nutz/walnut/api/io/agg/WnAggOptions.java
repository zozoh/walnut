package org.nutz.walnut.api.io.agg;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.Ws;

public class WnAggOptions {

    /**
     * 聚集计算函数
     */
    private WnAggFuncName funcName;

    /**
     * 聚集分组键名
     */
    private String groupBy;

    /**
     * 聚集计算键名
     */
    private String aggregateBy;

    /**
     * 聚集分组模式
     */
    private WnAggMode aggregateMode;

    /**
     * 聚集结果如何排序。 <code>null</code>表示不排序
     * 
     * @see org.nutz.walnut.api.io.agg.WnAggOrderBy
     */
    private WnAggOrderBy orderBy;

    /**
     * 升序还是降序
     * <ul>
     * <li><code>true</code> 升序（从小倒大）
     * <li><code>false</code> 降序（从大倒小）
     * </ul>
     */
    private boolean ASC;

    /**
     * 查找记录的最多限制。小于等于零表示全部数据
     */
    private int dataLimit;

    /**
     * 输出的统计数据最多限制。这个适合统计 Top10 之类的数据。小于等于零表示全部数据
     */
    private int outputLimit;

    public WnAggOptions() {
        this.funcName = WnAggFuncName.COUNT;
        this.orderBy = WnAggOrderBy.NAME;
        this.ASC = true;
        this.dataLimit = 0;
        this.outputLimit = 0;
    }

    public void assertValid() {
        if (null == funcName) {
            throw Er.create("e.io.agg.NoFuncName");
        }
        if (null == orderBy) {
            throw Er.create("e.io.agg.OrderBy");
        }
        if (Ws.isBlank(groupBy)) {
            throw Er.create("e.io.agg.GroupBy");
        }
        if (Ws.isBlank(aggregateBy)) {
            throw Er.create("e.io.agg.AggregateBy");
        }
    }

    public boolean isCOUNT() {
        return WnAggFuncName.COUNT == funcName;
    }

    public boolean isMAX() {
        return WnAggFuncName.MAX == funcName;
    }

    public boolean isMIN() {
        return WnAggFuncName.MIN == funcName;
    }

    public boolean isSUM() {
        return WnAggFuncName.SUM == funcName;
    }

    public boolean isAVG() {
        return WnAggFuncName.AVG == funcName;
    }

    public boolean hasFuncName() {
        return null != funcName;
    }

    public WnAggFuncName getFuncName() {
        return funcName;
    }

    public void setFuncName(WnAggFuncName type) {
        this.funcName = type;
    }

    public boolean hasGroupBy() {
        return !Ws.isBlank(groupBy);
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String key) {
        this.groupBy = key;
    }

    public boolean hasAggregateBy() {
        return !Ws.isBlank(aggregateBy);
    }

    public String getAggregateBy() {
        return aggregateBy;
    }

    public void setAggregateBy(String aggregateBy) {
        this.aggregateBy = aggregateBy;
    }

    public boolean is_mode_RAW() {
        return null == this.aggregateMode || WnAggMode.RAW == this.aggregateMode;
    }

    public boolean is_mode_TIMESTAMP_TO_DATE() {
        return WnAggMode.TIMESTAMP_TO_DATE == this.aggregateMode;
    }

    public boolean hasAggregateMode() {
        return null != this.aggregateMode;
    }

    public WnAggMode getAggregateMode() {
        return aggregateMode;
    }

    public void setAggregateMode(WnAggMode keyType) {
        this.aggregateMode = keyType;
    }

    public boolean hasOrderBy() {
        return null != orderBy;
    }

    public String getOrderKey(String nameKey, String valueKey) {
        if (WnAggOrderBy.NAME == this.orderBy) {
            return nameKey;
        }
        return valueKey;
    }

    public WnAggOrderBy getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(WnAggOrderBy orderBy) {
        this.orderBy = orderBy;
    }

    public boolean isASC() {
        return ASC;
    }

    public void asASC() {
        this.ASC = true;
    }

    public void asDESC() {
        this.ASC = false;
    }

    public void setASC(boolean asc) {
        this.ASC = asc;
    }

    public <T> T getOrderVal(T ascVal, T descVal) {
        if (this.ASC) {
            return ascVal;
        }
        return descVal;
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
