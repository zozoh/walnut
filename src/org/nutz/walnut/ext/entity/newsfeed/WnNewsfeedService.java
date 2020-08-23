package org.nutz.walnut.ext.entity.newsfeed;

import java.util.ArrayList;
import java.util.List;

import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.Dao;
import org.nutz.dao.QueryResult;
import org.nutz.dao.TableName;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.lang.Strings;
import org.nutz.trans.Proton;
import org.nutz.walnut.ext.sql.WnDaoConfig;
import org.nutz.walnut.util.Wn;

public class WnNewsfeedService implements NewsfeedApi {

    /**
     * 配置对象
     */
    WnDaoConfig config;

    /**
     * SQL数据库操作接口
     */
    private Dao dao;

    public WnNewsfeedService(WnDaoConfig config, Dao dao) {
        this.config = config;
        this.dao = dao;
    }

    @Override
    public Newsfeed fetch(String id) {
        String tableName = config.getTableName();
        Newsfeed feed = TableName.run(tableName, new Proton<Newsfeed>() {
            protected Newsfeed exec() {
                return dao.fetch(Newsfeed.class, id);
            }
        });
        return feed;
    }

    @Override
    public void update(Newsfeed feed, String... fields) {
        String tableName = config.getTableName();
        TableName.run(tableName,
                      () -> {
                          // 更新局部字段
                          if (fields.length > 0) {
                              String actived = "^" + Strings.join("|", fields) + "$";
                              dao.update(feed, actived);
                          }
                          // 更新全部字段
                          else {
                              dao.update(feed);
                          }
                      });
    }

    @Override
    public Newsfeed setReaded(String id, boolean readed) {
        // 获取
        Newsfeed feed = this.fetch(id);
        // 设置
        feed.setReaded(readed);
        feed.setReadAt(Wn.now());
        // 更新
        this.update(feed, "read", "readAt");
        // 返回
        return feed;
    }

    @Override
    public int setAllReaded(String targetId, boolean readed) {
        String tableName = config.getTableName();
        Chain chain = Chain.make("readed", readed);
        Cnd cnd = Cnd.where("ta_id", "=", targetId).andNot("readed", "=", readed);
        int n = dao.update(tableName, chain, cnd);
        return n;
    }

    @Override
    public Newsfeed setStared(String id, boolean stared) {
        // 获取
        Newsfeed feed = this.fetch(id);
        // 设置
        feed.setStared(stared);
        // 更新
        this.update(feed, "stared");
        // 返回
        return feed;
    }

    @Override
    public int cleanAllReaded(String targetId) {
        Cnd cnd = Cnd.where("targetId", "=", targetId)
                     .and("stared", "=", false)
                     .and("readed", "=", true);
        String tableName = config.getTableName();
        int n = TableName.run(tableName, new Proton<Integer>() {
            protected Integer exec() {
                return dao.clear(Newsfeed.class, cnd);
            }
        });
        return n;
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
                    return dao.delete(Newsfeed.class, ids[0]);
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
    public QueryResult query(FeedQuery q, int pn, int pgsz) {
        // 准备条件
        Pager pager = dao.createPager(pn, pgsz);
        Condition cnd = null != q ? q.toCondition() : null;

        // 查询
        String tableName = config.getTableName();
        List<Newsfeed> list = TableName.run(tableName, new Proton<List<Newsfeed>>() {
            protected List<Newsfeed> exec() {
                int count = dao.count(Newsfeed.class, cnd);
                pager.setRecordCount(count);
                return dao.query(Newsfeed.class, cnd, pager);
            }
        });

        // 返回
        return new QueryResult(list, pager);
    }

    @Override
    public Newsfeed add(Newsfeed feed) {
        // 自动补全
        feed.autoComplete(true);

        // 自动分配 ID
        if (!feed.hasId()) {
            feed.setId(Wn.genId());
        }

        // 执行插入
        String tableName = config.getTableName();
        Newsfeed re = TableName.run(tableName, new Proton<Newsfeed>() {
            protected Newsfeed exec() {
                return dao.insert(feed);
            }
        });

        // 返回
        return re;
    }

    @Override
    public List<Newsfeed> batchAdd(Newsfeed feed, String[] targetIds) {
        // 防守
        if (null == targetIds || targetIds.length == 0) {
            return new ArrayList<>();
        }

        // 自动补全模板
        feed.autoComplete(true);

        // 准备插入对象
        List<Newsfeed> list = new ArrayList<>(targetIds.length);
        for (String taId : targetIds) {
            Newsfeed fd = feed.clone();
            fd.setId(Wn.genId());
            fd.setTargetId(taId);
            fd.autoComplete(true);
            list.add(fd);
        }

        // 批量插入
        String tableName = config.getTableName();
        List<Newsfeed> reList = TableName.run(tableName, new Proton<List<Newsfeed>>() {
            protected List<Newsfeed> exec() {
                return dao.fastInsert(list);
            }
        });

        // 搞定
        return reList;
    }

}
