package org.nutz.walnut.ext.sys.sql;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class WnDataSource implements DataSource {

    private DataSource ds;

    public WnDataSource(DataSource ds) {
        this.ds = ds;
    }

    private Connection wrap(Connection conn) {
        return new WnConnection(conn);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return ds.unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return ds.isWrapperFor(iface);
    }

    public Connection getConnection() throws SQLException {
        return wrap(ds.getConnection());
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return wrap(ds.getConnection(username, password));
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return ds.getParentLogger();
    }

    public PrintWriter getLogWriter() throws SQLException {
        return ds.getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        ds.setLogWriter(out);
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        ds.setLoginTimeout(seconds);
    }

    public int getLoginTimeout() throws SQLException {
        return ds.getLoginTimeout();
    }

}
