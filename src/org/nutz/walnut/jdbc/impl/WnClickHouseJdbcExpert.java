package org.nutz.walnut.jdbc.impl;

public class WnClickHouseJdbcExpert extends WnAbstractJdbcExpert {

    @Override
    public String funcTimestampToDate(String key) {
        return "toDate(" + key + "/1000)";
    }

}
