package com.site0.walnut.ext.data.sqlx.util;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface SqlResetSetGetter<T> {

    T getValue(ResultSet rs) throws SQLException;

}
