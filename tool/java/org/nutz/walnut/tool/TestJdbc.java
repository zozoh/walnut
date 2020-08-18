package org.nutz.walnut.tool;

import java.sql.Connection;
import java.sql.SQLException;

import org.nutz.dao.Dao;
import org.nutz.dao.impl.NutDao;

import com.alibaba.druid.pool.DruidDataSource;

public class TestJdbc {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/walnut_ti?zeroDateTimeBehavior=convertToNull&serverTimezone=GMT%2b8");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        dataSource.setMaxActive(50);
        dataSource.setMaxWait(15000);
        dataSource.setTestWhileIdle(true);

        Connection conn = dataSource.getConnection();
        Dao dao = new NutDao(dataSource);
        dao.exists("t_abc");
        conn.close();
    }

}
