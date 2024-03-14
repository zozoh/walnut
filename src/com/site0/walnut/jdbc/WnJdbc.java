package com.site0.walnut.jdbc;

import java.util.HashMap;
import java.util.Map;

import com.site0.walnut.jdbc.impl.WnClickHouseJdbcExpert;
import com.site0.walnut.jdbc.impl.WnMySqlJdbcExpert;

public abstract class WnJdbc {

    private static final Map<String, WnJdbcExpert> experts = new HashMap<>();

    static {
        experts.put("MySQL", new WnMySqlJdbcExpert());
        experts.put("ClickHouse", new WnClickHouseJdbcExpert());
    }

    /**
     * 获取一个数据库专家类
     * 
     * @param name
     *            数据库产品名称， 通过
     *            <code>DatabaseMetaData.getDatabaseProductName</code> 获得
     * @param dft
     *            默认数据库产品名称。如果指定名称没有找到专家类，用什么已知产品来替代。 <br>
     *            通常，是用 <code>MySQL</code> 来代替的
     * @return 数据库专家类
     * 
     * @see java.sql.DatabaseMetaData#getDatabaseProductName()
     */
    public static WnJdbcExpert getExpert(String name, String dft) {
        WnJdbcExpert je = experts.get(name);
        if (null == je) {
            je = experts.get(dft);
        }
        return je;
    }

    /**
     * 获取一个数据库专家类，如果找不到，则用<code>MySQL</code>来代替。
     * 
     * @param name
     *            数据库产品名称， 通过
     *            <code>DatabaseMetaData.getDatabaseProductName</code> 获得
     * @return 数据库专家类
     * @see #getExpert(String, String)
     */
    public static WnJdbcExpert getExpert(String name) {
        return getExpert(name, "MySQL");
    }
}
