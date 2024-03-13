package org.nutz.walnut.ext.data.sqlx.processor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.log.Log;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.Wlog;

public class QueryProcessor implements SqlProcessor<List<NutBean>> {

    private static Log log = Wlog.getCMD();

    @Override
    public List<NutBean> run(Connection conn, String sql) {
        // TODO 请帮我实现这个函数。 NutBean 可以调用 this.toBean(ResultSet)
        // 来转换
        List<NutBean> list = new LinkedList<>();
        if (log.isInfoEnabled()) {
            log.info(Wlog.msg(sql));
        }

        try {
            // 创建一个 Statement 对象来执行 SQL 查询
            Statement sta = conn.createStatement();

            // 执行 SQL 查询，并得到结果集
            ResultSet resultSet = sta.executeQuery(sql);

            // 遍历结果集
            while (resultSet.next()) {
                // 调用 toBean 方法将结果转换为 NutBean 对象，并将其添加到结果列表中
                NutBean nutBean = toBean(resultSet);
                list.add(nutBean);
            }

            // 关闭结果集和语句
            resultSet.close();
            sta.close();
        }
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warnf("SQL Fail: %s", sql);
            }
            throw Er.wrap(e);
        }
        return list;
    }

    @Override
    public List<NutBean> runWithParams(Connection conn, String sql, Object[] params) {
        return null;
    }

    @Override
    public List<NutBean> batchRun(Connection conn, String sql, List<Object[]> params) {
        return null;
    }

    private NutBean toBean(ResultSet rs) {
        return null;
    }

}
