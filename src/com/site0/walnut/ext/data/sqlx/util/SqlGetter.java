package com.site0.walnut.ext.data.sqlx.util;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlGetter<T> {

    T doGet(Connection conn) throws SQLException;

}
