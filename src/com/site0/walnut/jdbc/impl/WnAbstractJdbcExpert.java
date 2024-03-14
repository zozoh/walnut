package com.site0.walnut.jdbc.impl;

import com.site0.walnut.jdbc.WnJdbcExpert;

public abstract class WnAbstractJdbcExpert implements WnJdbcExpert {

    @Override
    public String funcAggCount(String key) {
        return "COUNT(" + key + ")";
    }

    @Override
    public String funcAggMax(String key) {
        return "MAX(" + key + ")";
    }

    @Override
    public String funcAggMin(String key) {
        return "MIN(" + key + ")";
    }

    @Override
    public String funcAggAvg(String key) {
        return "AVG(" + key + ")";
    }

    @Override
    public String funcAggSum(String key) {
        return "SUM(" + key + ")";
    }

}
