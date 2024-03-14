package com.site0.walnut.jdbc.impl;

public class WnClickHouseJdbcExpert extends WnAbstractJdbcExpert {

    @Override
    public String funcTimestampToDate(String key) {
        return "toDate(" + key + "/1000)";
    }

}
