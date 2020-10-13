package org.nutz.walnut.ext.entity.history;

import java.util.List;

import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.Dao;
import org.nutz.dao.QueryResult;
import org.nutz.dao.TableName;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.trans.Proton;
import org.nutz.walnut.ext.sql.WnDaoConfig;
import org.nutz.walnut.util.Wn;

public class WnHistoryService implements HistoryApi {

    /**
     * 配置对象
     */
    WnDaoConfig config;

    /**
     * SQL数据库操作接口
     */
    private Dao dao;

    public WnHistoryService(WnDaoConfig config, Dao dao) {
        this.config = config;
        this.dao = dao;
    }

    @Override
    public HistoryRecord fetch(String id) {
        String tableName = config.getTableName();
        return TableName.run(tableName, new Proton<HistoryRecord>() {
            protected HistoryRecord exec() {
                return dao.fetch(HistoryRecord.class, id);
            }
        });
    }

    @Override
    public int remove(String... ids) {
        if (ids.length <= 0) {
            return 0;
        }

        String tableName = config.getTableName();

        // 移除一条
        if (ids.length == 0) {
            return TableName.run(tableName, new Proton<Integer>() {
                protected Integer exec() {
                    return dao.delete(HistoryRecord.class, ids[0]);
                }
            });
        }

        // 一次移除多个
        SqlExpressionGroup we = new SqlExpressionGroup();
        we.andIn("id", ids);
        Cnd cnd = Cnd.where(we);
        return dao.clear(tableName, cnd);
    }

    @Override
    public int removeBy(HisQuery q) {
        Condition cnd = null != q ? q.toCondition() : null;
        String tableName = config.getTableName();
        return TableName.run(tableName, new Proton<Integer>() {
            protected Integer exec() {
                return dao.clear(HistoryRecord.class, cnd);
            }
        });
    }

    @Override
    public QueryResult query(HisQuery q, int pn, int pgsz) {
        // 准备条件
        Pager pager = dao.createPager(pn, pgsz);
        Condition cnd = null != q ? q.toCondition() : null;

        // 查询
        String tableName = config.getTableName();
        List<HistoryRecord> list = TableName.run(tableName, new Proton<List<HistoryRecord>>() {
            protected List<HistoryRecord> exec() {
                int count = dao.count(HistoryRecord.class, cnd);
                pager.setRecordCount(count);
                return dao.query(HistoryRecord.class, cnd, pager);
            }
        });

        // 返回
        return new QueryResult(list, pager);
    }

    @Override
    public HistoryRecord add(HistoryRecord his) {
        // 自动设置创建时间
        if (!his.hasCreateTime()) {
            his.setCreateTime(Wn.now());
        }

        // 自动分配 ID
        if (!his.hasId()) {
            his.setId(Wn.genId());
        }

        // 执行插入
        String tableName = config.getTableName();
        HistoryRecord re = TableName.run(tableName, new Proton<HistoryRecord>() {
            protected HistoryRecord exec() {
                return dao.insert(his);
            }
        });

        // 返回
        return re;
    }

}
