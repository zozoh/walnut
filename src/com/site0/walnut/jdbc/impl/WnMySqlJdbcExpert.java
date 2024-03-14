package com.site0.walnut.jdbc.impl;

public class WnMySqlJdbcExpert extends WnAbstractJdbcExpert {

    @Override
    public String funcTimestampToDate(String key) {
        return "FROM_UNIXTIME(" + key + "/1000, '%Y-%m-%d')";
    }

}
