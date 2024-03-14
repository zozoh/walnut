package com.site0.walnut.api.io.agg;

public class WnAggregateKey extends WnAggKey {

    /**
     * 聚集分组的值转换方式
     */
    private WnAggFunc func;

    public boolean hasFunc() {
        return null != func;
    }

    public WnAggFunc getFunc() {
        return func;
    }

    public void setFunc(WnAggFunc func) {
        this.func = func;
        if (null != func) {
            this.funcName = func.name();
        }
    }

}
