package org.nutz.walnut.core.indexer.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.castor.Castors;
import org.nutz.dao.Chain;
import org.nutz.dao.Condition;
import org.nutz.dao.Dao;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.entity.Record;
import org.nutz.dao.jdbc.JdbcExpert;
import org.nutz.dao.pager.Pager;
import org.nutz.lang.Each;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnIoIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.core.indexer.AbstractIoDataIndexer;
import org.nutz.walnut.ext.sql.WnDaoConfig;
import org.nutz.walnut.ext.sql.WnDaos;
import org.nutz.walnut.util.Wn;

public class DaoIndexer extends AbstractIoDataIndexer {

    private Dao dao;

    private WnObjEntity entity;

    public DaoIndexer(WnObj root, MimeMap mimes, WnDaoConfig config) {
        super(root, mimes);
        this.dao = WnDaos.get(config);

        // TODO 通过 config 生成 Entity
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
    protected WnObj _fetch_by_name(WnObj p, String name) {
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
    protected int _each(WnQuery q, Each<WnObj> callback) {
        // 木有必要查询
        if (null == callback) {
            return 0;
        }
        // 处理查询条件
        WnDaoQuery dq = genDaoQuery(q);
        Condition cond = dq.getCondition();
        Pager page = dq.getPager();

        // 执行查询
        final WnIoIndexer indexer = this;
        return dao.each(entity, cond, page, new Each<WnIoObj>() {
            public void invoke(int index, WnIoObj o, int length) {
                if (null != o) {
                    o.setIndexer(indexer);
                }
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
        if (null != o) {
            o.putAll(map);

            // 准备键锁
            List<String> fnms = new ArrayList<>(map.size());
            for (String k : map.keySet()) {
                // 直接更新的键
                if (entity.getField(k) != null) {
                    fnms.add(k);
                }
                // 否则就是收缩的键，当然，你必须是先在定义里声明了这个字段才有效
                else {
                    fnms.add("[.][.]");
                }
            }
            String actived = "^(" + Strings.join("|", fnms) + ")$";

            // 执行
            dao.update(entity, o, actived);

            if (returnNew) {
                WnIoObj o3 = dao.fetch(entity, cnd);
                o3.setIndexer(this);
                return o3;
            }
        }
        return old;
    }

}
