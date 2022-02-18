package org.nutz.walnut.core.indexer.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.nutz.castor.Castors;
import org.nutz.dao.Chain;
import org.nutz.dao.Condition;
import org.nutz.dao.Dao;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.impl.NutDao;
import org.nutz.dao.jdbc.JdbcExpert;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.pager.ResultSetLooping;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.sql.SqlCallback;
import org.nutz.dao.sql.SqlContext;
import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Trans;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnIoIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.agg.WnAggGroupKey;
import org.nutz.walnut.api.io.agg.WnAggOptions;
import org.nutz.walnut.api.io.agg.WnAggOrderBy;
import org.nutz.walnut.api.io.agg.WnAggResult;
import org.nutz.walnut.api.io.agg.WnAggTransMode;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.core.indexer.AbstractIoDataIndexer;
import org.nutz.walnut.ext.sys.sql.WnDaoMappingConfig;
import org.nutz.walnut.ext.sys.sql.WnDaos;
import org.nutz.walnut.jdbc.WnJdbc;
import org.nutz.walnut.jdbc.WnJdbcExpert;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.util.Ws;

public class DaoIndexer extends AbstractIoDataIndexer {

    private Dao dao;

    private WnObjEntity entity;

    private String dbProductName;

    private WnJdbcExpert expert;

    public DaoIndexer(WnObj root, MimeMap mimes, WnDaoMappingConfig config) {
        super(root, mimes);
        Dao dao = WnDaos.get(config.getAuth());

        // 获取数据库类型
        initDbProductName(dao);

        // 通过 config 生成 Entity
        JdbcExpert expert = dao.getJdbcExpert();
        WnObjEntityGenerating ing = new WnObjEntityGenerating(root, config, expert);
        this.entity = ing.generate();

        // 自动创建创建表
        if (config.isAutoCreate()) {
            if (!dao.exists(entity)) {
                dao.create(entity, false);
            }
        }
    }

    @Override
    public WnAggResult aggregate(WnQuery q, WnAggOptions agg) {
        agg.assertValid();
        String tableName = this.entity.getTableName();
        WnDaoQuery dq = genDaoQuery(q);
        Condition cond = dq.getCondition();
        String where = Ws.trim(cond.toSql(entity));

        // 准备上下文
        NutMap ctx = new NutMap();
        ctx.put("table", tableName);
        ctx.put("AGG_KEY_FROM", agg.getAggregateBy().getFromName());
        ctx.put("AGG_KEY_TO", agg.getAggregateBy().getToName());

        //
        // 准备聚集函数
        //
        if (agg.isCOUNT()) {
            ctx.put("FUNC", expert.funcAggCount("v0"));
        }
        // 最大值
        else if (agg.isMAX()) {
            ctx.put("FUNC", expert.funcAggMax("v0"));
        }
        // 最小值
        else if (agg.isMIN()) {
            ctx.put("FUNC", expert.funcAggMin("v0"));
        }
        // 平均数
        else if (agg.isAVG()) {
            ctx.put("FUNC", expert.funcAggAvg("v0"));
        }
        // 求和
        else if (agg.isSUM()) {
            ctx.put("FUNC", expert.funcAggSum("v0"));
        }
        // 不支持
        else {
            throw Er.create("e.io.agg.invalidAggFunc", agg.getFuncName());
        }

        //
        // 键的聚合前计算方法
        //
        List<String> group_out = new ArrayList<>(agg.getGroupBy().size());
        List<String> group_ins = new ArrayList<>(agg.getGroupBy().size());
        List<String> group_bys = new ArrayList<>(agg.getGroupBy().size());
        int i = 0;
        for (WnAggGroupKey gk : agg.getGroupBy()) {
            String tmpKey = "k" + (i++);
            // 转换时间戳到日期以便按日分组
            if (WnAggTransMode.TIMESTAMP_TO_DATE == gk.getFunc()) {
                String trans = expert.funcTimestampToDate(gk.getFromName());
                group_ins.add(String.format("%s AS %s", trans, tmpKey));
            } else {
                group_ins.add(String.format("%s AS %s", gk.getFromName(), tmpKey));
            }
            // 输出的分组字段
            group_out.add(String.format("%s AS %s", tmpKey, gk.getToName()));
            // 分组键
            group_bys.add(tmpKey);
        }
        ctx.put("GRP_INS", Ws.join(group_ins, ", "));
        ctx.put("GRP_OUT", Ws.join(group_out, ", "));
        ctx.put("GRP_BYS", Ws.join(group_bys, ", "));

        // 查询限制数量
        if (agg.hasDataLimit()) {
            ctx.put("LIMIT_DATA", String.format("LIMIT %d", agg.getDataLimit()));
        }
        // 查询限制条件
        if (null != where && !"WHERE".equals(where)) {
            ctx.put("WHERE", where);
        }
        if (agg.hasOutputLimit()) {
            ctx.put("LIMIT_OUTPUT", String.format("LIMIT %d", agg.getOutputLimit()));
        }

        // 求和结果的排序
        if (agg.hasOrderBy()) {
            List<String> sorting = new ArrayList<>(agg.getOrderBy().size());
            for (WnAggOrderBy ob : agg.getOrderBy()) {
                if (ob.isAsc()) {
                    sorting.add(String.format("%s ASC", ob.getName()));
                } else {
                    sorting.add(String.format("%s DESC", ob.getName()));
                }
            }
            ctx.put("ORDER_BY", String.format("ORDER BY %s", Ws.join(sorting, ", ")));
        }

        // 拼 SQL
        String str = Tmpl.exec("SELECT "
                               + "  ${GRP_OUT},"
                               + "  ${FUNC} AS ${AGG_KEY_TO} "
                               + "FROM"
                               + "("
                               + "    SELECT "
                               + "      ${AGG_KEY_FROM} AS v0,"
                               + "      ${GRP_INS}"
                               + "    FROM ${table} "
                               + "    ${WHERE?} ${LIMIT_DATA?}"
                               + ") AS result "
                               + "GROUP BY ${GRP_BYS} "
                               + "${ORDER_BY} "
                               + "${LIMIT_OUTPUT?} ;",
                               ctx);

        // 准备返回结果
        WnAggResult re = new WnAggResult();

        // 准备 SQL
        ResultSetMetaData[] rsms = new ResultSetMetaData[1];
        Sql sql = Sqls.create(str);
        sql.setCallback(new SqlCallback() {
            public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
                ResultSetLooping ing = new ResultSetLooping() {
                    protected boolean createObject(int index,
                                                   ResultSet rs,
                                                   SqlContext context,
                                                   int rowCount) {
                        try {
                            // 得到元数据
                            ResultSetMetaData rsm = rsms[0];
                            if (null == rsm) {
                                rsm = rs.getMetaData();
                                rsms[0] = rsm;
                            }

                            NutBean bean = new NutMap();

                            // 记录每一个字段
                            int count = rsm.getColumnCount();
                            for (int i = 1; i <= count; i++) {
                                String key = rsm.getColumnName(i);
                                int colType = rsm.getColumnType(i);
                                Object val;
                                switch (colType) {
                                case Types.BIGINT:
                                    val = rs.getBigDecimal(i);
                                    break;
                                case Types.INTEGER:
                                    val = rs.getLong(i);
                                    break;
                                case Types.SMALLINT:
                                case Types.TINYINT:
                                    val = rs.getInt(i);
                                    break;
                                case Types.DOUBLE:
                                    val = rs.getDouble(i);
                                    break;
                                case Types.FLOAT:
                                    val = rs.getFloat(i);
                                    break;
                                case Types.BOOLEAN:
                                    val = rs.getBoolean(i);
                                    break;
                                default:
                                    val = rs.getString(i);
                                }
                                // 计入返回值
                                bean.put(key, val);
                            }

                            // 计入返回结果
                            re.add(bean);
                        }
                        catch (SQLException e) {
                            throw Lang.wrapThrow(e);
                        }
                        return true;
                    }
                };

                ing.doLoop(rs, sql.getContext());
                return null;
            }
        });

        // 执行 SQL
        dao.execute(sql);

        // 搞定
        return re;
    }

    @Override
    protected WnObj _fetch_by_name(WnObj p, String name) {
        // 如果映射中没有 nm 字段，则一律返回 null
        if (!entity.hasNameField()) {
            return null;
        }

        WnQuery q = Wn.Q.pid(p);
        q.setv("nm", name);
        q.limit(1);
        return this.getOne(q);
    }

    @Override
    public int inc(WnQuery q, String key, int val, boolean returnNew) {
        final int[] vs = new int[1];
        this.each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj o, int length) {
                vs[0] = o.getInt(key);
                o.intIncrement(key, val);
                WnIoObj io = (WnIoObj) o;
                dao.update(entity, io, "^(" + key + ")$");
                if (returnNew)
                    vs[0] = o.getInt(key);
            }
        });
        if (returnNew) {
            WnObj o = this.getOne(q);
            return o.getInt(key);
        }
        return vs[0];
    }

    private WnDaoQuery genDaoQuery(WnQuery q) {
        return new WnDaoQuery(q, entity);
    }

    @Override
    public <T> T getAs(String id, String key, Class<T> classOfT, T dft) {
        // 木有声明的键，全都获取出来，再读取
        if (null == entity.getField(key)) {
            WnObj o = this.get(id);
            if (null == o) {
                return dft;
            }
            return o.getAs(key, classOfT, dft);
        }

        WnQuery q = Wn.Q.id(id);
        WnDaoQuery dq = genDaoQuery(q);
        Condition cnd = dq.getCondition();

        Record re = dao.fetch(entity.getViewName(), cnd, key);
        if (null == re)
            return dft;

        Object val = re.get(key);
        if (null == val)
            return dft;

        return Castors.me().castTo(val, classOfT);
    }

    @Override
    public void delete(WnObj o) {
        dao.delete(entity, o.myId());
    }

    @Override
    protected WnIoObj _get_by_id(String id) {
        // 如果是不完整的 ID ...
        if (!Wn.isFullObjId(id)) {

            // 处理查询条件
            WnQuery q = new WnQuery().limit(2);
            q.setv("id", id + "*");
            WnDaoQuery dq = genDaoQuery(q);
            Condition cond = dq.getCondition();
            Pager page = dq.getPager();

            // 执行查询
            List<WnIoObj> list = dao.query(entity, cond, page);
            if (list.isEmpty())
                return null;
            if (list.size() > 1)
                throw Er.create("e.io.obj.get.shortid", id);
            return list.get(0);
        }

        // 那就是完整的 ID 咯
        return dao.fetch(entity, id);
    }

    @Override
    protected int _each(WnQuery q, WnObj pHint, Each<WnObj> callback) {
        // 木有必要查询
        if (null == callback) {
            return 0;
        }

        // 得到节点检查的回调接口
        WnContext wc = Wn.WC();

        // 处理查询条件
        WnDaoQuery dq = genDaoQuery(q);
        Condition cond = dq.getCondition();
        Pager page = dq.getPager();

        // 执行查询
        final WnIoIndexer indexer = this;
        return dao.each(entity, cond, page, new Each<WnIoObj>() {
            public void invoke(int index, WnIoObj o, int length) {
                if (null == o) {
                    return;
                }
                // 根据父对象完成自身未设置的字段
                o.setIndexer(indexer);
                _complete_obj_by_parent(pHint, o);

                // 确保可读
                o = (WnIoObj) wc.whenAccess(o, true);
                if (null == o) {
                    return;
                }

                // 自动读取路径
                if (wc.isAutoPath()) {
                    o.path();
                }

                // 通知回调
                callback.invoke(index, o, length);
            }
        });
    }

    @Override
    public long count(WnQuery q) {
        // 处理查询条件
        WnDaoQuery dq = genDaoQuery(q);
        Condition cond = dq.getCondition();

        // 计算
        return dao.count(entity, cond);
    }

    @Override
    public WnObj push(String id, String key, Object val, boolean returnNew) {
        // 读取出来
        WnIoObj o = dao.fetch(entity, id);
        if (null == o) {
            return null;
        }

        // 设置值
        o.pushTo(key, val);

        // 尝试更新
        dao.update(entity, o, FieldFilter.create(entity.getType(), "^(" + key + ")$"));

        // 查询回来
        if (returnNew) {
            return dao.fetch(entity, id);
        }

        // 直接返回
        o.setIndexer(this);
        return o;
    }

    @Override
    public void push(WnQuery query, String key, Object val) {
        // 准备条件
        WnDaoQuery dq = genDaoQuery(query);
        Condition cnd = dq.getCondition();
        Pager page = dq.getPager();

        // 读取出来
        List<WnIoObj> list = dao.query(entity, cnd, page);

        if (list.isEmpty()) {
            return;
        }

        // 设置值
        for (WnIoObj o : list) {
            o.pushTo(key, val);
        }

        // 尝试更新
        dao.update(entity, list, FieldFilter.create(entity.getType(), "^(" + key + ")$"));

    }

    @Override
    public WnObj pull(String id, String key, Object val, boolean returnNew) {
        // 读取出来
        WnIoObj o = dao.fetch(entity, id);
        if (null == o) {
            return null;
        }

        if (null == val) {
            return o;
        }

        // 设置值
        List<Object> list = o.getList(key, Object.class);
        if (null == list || list.isEmpty()) {
            return o;
        }
        int oldSize = list.size();
        if (null != list)
            list.remove(val);

        // 木有变化
        if (oldSize == list.size()) {
            return o;
        }

        // 尝试更新
        o.put(key, list);
        dao.update(entity, o, FieldFilter.create(entity.getType(), "^(" + key + ")$"));

        // 查询回来
        if (returnNew) {
            return dao.fetch(entity, id);
        }

        // 直接返回
        o.setIndexer(this);
        return o;
    }

    @Override
    public void pull(WnQuery query, String key, Object val) {
        // 准备条件
        WnDaoQuery dq = genDaoQuery(query);
        Condition cnd = dq.getCondition();
        Pager page = dq.getPager();

        // 读取出来
        List<WnIoObj> list = dao.query(entity, cnd, page);
        List<WnIoObj> list2 = new ArrayList<>();

        if (list.isEmpty()) {
            return;
        }

        // 设置值
        for (WnIoObj o : list) {
            // 设置值
            List<Object> vals = o.getList(key, Object.class);
            if (null == vals)
                continue;
            int oldSize = vals.size();
            vals.remove(val);

            // 木有变化
            if (oldSize != vals.size()) {
                o.put(key, vals);
                list2.add(o);
            }
        }

        // 防守
        if (list2.isEmpty()) {
            return;
        }

        // 尝试更新
        dao.update(entity, list2, FieldFilter.create(entity.getType(), "^(" + key + ")$"));
    }

    @Override
    protected WnObj _create(WnIoObj o) {
        if (!o.hasParent()) {
            o.setParent(this.root);
        }
        o.setIndexer(this);
        return dao.insert(entity, o);
    }

    @Override
    protected void _set(String id, NutBean map) {
        WnQuery q = Wn.Q.id(id);
        // 准备值链
        Chain chain = null;
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            // 木有字段的，不能直接更新，那么用 _set_by 吧
            if (null == entity.getField(key)) {
                _set_by(q, map, false);
                return;
            }
            if (null == chain) {
                chain = Chain.make(key, val);
            } else {
                chain.add(key, val);
            }
        }
        chain.updateBy(entity);

        // 准备查询条件
        WnDaoQuery dq = genDaoQuery(q);
        Condition cnd = dq.getCondition();

        // 执行更新
        dao.update(entity, chain, cnd);
    }

    @Override
    protected WnIoObj _set_by(WnQuery q, NutBean map, boolean returnNew) {
        WnDaoQuery dq = genDaoQuery(q);
        Condition cnd = dq.getCondition();

        // 先读取出来
        WnIoObj o = dao.fetch(entity, cnd);
        o.setIndexer(this);
        WnIoObj old = (WnIoObj) o.clone();
        if (null != o && null != map && !map.isEmpty()) {
            o.putAll(map);

            // 准备键锁
            List<String> fnms = new ArrayList<>(map.size());
            int updateFieldCount = 0;
            boolean hasMoreFields = entity.getField("..") != null;
            for (String k : map.keySet()) {
                // 直接更新的键
                if (entity.getField(k) != null) {
                    fnms.add(k);
                    updateFieldCount++;
                }
                // 否则就是收缩的键，当然，你必须是先在定义里声明了这个字段才有效
                else if (hasMoreFields) {
                    fnms.add("[.][.]");
                    updateFieldCount++;
                }
            }
            String actived = "^(" + Strings.join("|", fnms) + ")$";

            // 如果有字段要更新，就执行
            if (updateFieldCount > 0) {
                dao.update(entity, o, actived);

                if (returnNew) {
                    WnIoObj o3 = dao.fetch(entity, cnd);
                    o3.setIndexer(this);
                    return o3;
                }
            }
        }
        return old;
    }

    private void initDbProductName(Dao dao) {
        this.dao = dao;
        DataSource ds = ((NutDao) dao).getDataSource();
        Connection conn = null;
        try {
            conn = Trans.getConnectionAuto(ds);
            DatabaseMetaData meta = conn.getMetaData();
            this.dbProductName = meta.getDatabaseProductName();
            this.expert = WnJdbc.getExpert(this.dbProductName);
        }
        catch (Throwable e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            Trans.closeConnectionAuto(conn);
        }
    }
}
