package com.site0.walnut.api.io.agg;

public class WnAggGroupKey extends WnAggKey {

    private WnAggTransMode func;

    public boolean hasFunc() {
        return null != func;
    }

    public WnAggTransMode getFunc() {
        return func;
    }

    public void setFunc(WnAggTransMode func) {
        this.func = func;
        if (null != func) {
            this.funcName = func.name();
        }
    }

}
