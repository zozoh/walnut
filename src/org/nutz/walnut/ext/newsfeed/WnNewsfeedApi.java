package org.nutz.walnut.ext.newsfeed;

import java.util.List;

import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.Dao;
import org.nutz.dao.QueryResult;
import org.nutz.dao.TableName;
import org.nutz.dao.pager.Pager;
import org.nutz.lang.Strings;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sql.WnDaos;

public class WnNewsfeedApi {

    /**
     * 获取接口的工厂方法
     * 
     * @param io
     *            Io 接口
     * @param se
     *            会话（以便获取配置信息）
     * @param configName
     *            配置文件名
     * @return WnNewsfeedApi 接口实例
     */
    public static WnNewsfeedApi getInstance(WnIo io, WnAuthSession se, String configName) {
        // 主目录
        WnObj oHome = io.check(null, se.getMe().getHomePath());
        WnObj oFeedHome = io.check(oHome, ".domain/newsfeed");

        // 配置对象
        if (!configName.endsWith(".json")) {
            configName += ".json";
        }
        WnObj oFeedConf = io.check(oFeedHome, configName);
        WnNewsfeedConfig feedConf = io.readJson(oFeedConf, WnNewsfeedConfig.class);

        // 数据源
        Dao dao = WnDaos.getOrCreate(feedConf.getJdbcUrl(),
                                                     feedConf.getJdbcUserName(),
                                                     feedConf.getJdbcPassword());

        // 返回
        return new WnNewsfeedApi(feedConf, dao);
    }

    /**
     * 配置对象
     */
    WnNewsfeedConfig config;

    /**
     * SQL数据库操作接口
     */
    private Dao dao;

    public WnNewsfeedApi(WnNewsfeedConfig config, Dao dao) {
        this.config = config;
        this.dao = dao;
    }

    public WnNewsfeed fetch(String id) {
        String tableName = config.getTableName();
        WnNewsfeed feed = TableName.run(tableName, new Proton<WnNewsfeed>() {
            protected WnNewsfeed exec() {
                return dao.fetch(WnNewsfeed.class, id);
            }
        });
        return feed;
    }

    public void update(WnNewsfeed feed, String... fields) {
        String tableName = config.getTableName();
        TableName.run(tableName, () -> {
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

    public WnNewsfeed setRead(String id, boolean read) {
        // 获取
        WnNewsfeed feed = this.fetch(id);
        // 设置
        feed.setRead(read);
        feed.setReadAt(System.currentTimeMillis());
        // 更新
        this.update(feed, "read", "readAt");
        // 返回
        return feed;
    }

    public int setAllRead(String targetId, boolean read) {
        String tableName = config.getTableName();
        Chain chain = Chain.make("read", read);
        Cnd cnd = Cnd.where("targetId", "=", targetId).and("read", "!=", read);
        int n = dao.update(tableName, chain, cnd);
        return n;
    }

    public WnNewsfeed setStar(String id, boolean star) {
        // 获取
        WnNewsfeed feed = this.fetch(id);
        // 设置
        feed.setStar(star);
        // 更新
        this.update(feed, "star");
        // 返回
        return feed;
    }

    public int cleanAllReaded(String targetId) {
        Cnd cnd = Cnd.where("targetId", "=", targetId).and("star", "=", 0).and("read", "=", 1);
        String tableName = config.getTableName();
        int n = TableName.run(tableName, new Proton<Integer>() {
            protected Integer exec() {
                return dao.clear(WnNewsfeed.class, cnd);
            }
        });
        return n;
    }

    /**
     * @param id
     *            消息 ID
     * @return true 表示删除成功
     */
    public boolean remove(String id) {
        String tableName = config.getTableName();
        int n = TableName.run(tableName, new Proton<Integer>() {
            protected Integer exec() {
                return dao.delete(WnNewsfeed.class, id);
            }
        });
        return n > 0;
    }

    /**
     * @param q
     *            查询条件
     * @param pn
     *            当前页（1 base）
     * @param pgsz
     *            页大小
     * @return 查询结果（当前页列表，以及翻页信息）
     */
    public QueryResult query(WnNewsfeedQuery q, int pn, int pgsz) {
        // 准备条件
        Pager pager = new Pager(pn, pgsz);
        Condition cnd = q.toCondition();

        // 查询
        String tableName = config.getTableName();
        List<WnNewsfeed> list = TableName.run(tableName, new Proton<List<WnNewsfeed>>() {
            protected List<WnNewsfeed> exec() {
                return dao.query(WnNewsfeed.class, cnd, pager);
            }
        });

        // 返回
        return new QueryResult(list, pager);
    }

    /**
     * 向消息流里插入一条信息对象
     * 
     * @param feed
     *            信息对象
     * @return 插入的信息对象
     */
    public WnNewsfeed add(WnNewsfeed feed) {
        // 自动补全
        feed.autoComplete(true);

        // 执行插入
        String tableName = config.getTableName();
        WnNewsfeed re = TableName.run(tableName, new Proton<WnNewsfeed>() {
            protected WnNewsfeed exec() {
                return dao.insert(feed);
            }
        });

        // 返回
        return re;
    }

}
