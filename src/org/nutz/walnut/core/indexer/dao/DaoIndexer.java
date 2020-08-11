package org.nutz.walnut.core.indexer.dao;

import java.util.ArrayList;
import java.util.List;

import org.nutz.castor.Castors;
import org.nutz.dao.Condition;
import org.nutz.dao.Dao;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.entity.Record;
import org.nutz.dao.pager.Pager;
import org.nutz.lang.Each;
import org.nutz.lang.util.NutBean;

import org.nutz.walnut.api.io.MimeMap;
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

    private WnDaoConfig config;

    protected DaoIndexer(WnObj root, MimeMap mimes, WnDaoConfig config) {
        super(root, mimes);
        this.dao = WnDaos.get(config);
        this.config = config;

        // TODO 通过 config 生成 Entity
        WnObjEntityGenerating ing = new WnObjEntityGenerating(config, dao.getJdbcExpert());
        this.entity = ing.generate();

        // 自动创建创建表
        if (config.isAutoCreate()) {
            if (dao.exists(entity)) {
                dao.create(entity, false);
            }
        }
    }

    @Override
    public WnObj fetchByName(WnObj p, String name) {
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
                o.intIncrement(key, val);
                WnIoObj io = (WnIoObj) o;
                dao.update(entity, io, "^(" + key + ")$");
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
        return new WnDaoQuery(q, entity, config);
    }

    @Override
    public <T> T getAs(String id, String key, Class<T> classOfT, T dft) {
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
        dao.delete(entity, o.id());
    }

    @Override
    public WnObj get(String id) {
        return dao.fetch(entity, id);
    }

    @Override
    public int each(WnQuery q, Each<WnObj> callback) {
        // 木有必要查询
        if (null == callback) {
            return 0;
        }
        // 处理查询条件
        WnDaoQuery dq = genDaoQuery(q);
        Condition cond = dq.getCondition();
        Pager page = dq.getPager();

        // 执行查询
        return dao.each(entity, cond, page, new Each<WnIoObj>() {
            public void invoke(int index, WnIoObj o, int length) {
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
            int oldSize = list.size();
            if (null != list || list.isEmpty())
                list.remove(val);

            // 木有变化
            if (oldSize != list.size()) {
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
        return dao.insert(entity, o);
    }

    @Override
    protected void _set(String id, NutBean map) {
        WnQuery q = Wn.Q.id(id);
        WnDaoQuery dq = genDaoQuery(q);
        Condition cnd = dq.getCondition();
        WnIoObj o = new WnIoObj();
        o.putAll(map);
        dao.update(entity, o, cnd);
    }

    @Override
    protected WnIoObj _set_by(WnQuery q, NutBean map, boolean returnNew) {
        WnDaoQuery dq = genDaoQuery(q);
        Condition cnd = dq.getCondition();
        WnIoObj o = new WnIoObj();
        o.putAll(map);
        dao.update(entity, o, cnd);

        if (returnNew)
            return dao.fetch(entity, cnd);

        return o;
    }

}
