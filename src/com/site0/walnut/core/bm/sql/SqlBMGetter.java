package com.site0.walnut.core.bm.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlBMGetter<T> {

    T doGet(Connection conn) throws SQLException;

}
