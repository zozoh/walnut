package org.nutz.walnut.ext.data.sqlx.processor;

import java.sql.Connection;
import java.util.List;

public interface SqlProcessor<T> {

    T run(Connection conn, String sql);

    T runWithParams(Connection conn, String sql, Object[] params);
    
    T batchRun(Connection conn, String sql, List<Object[]> params);
}
