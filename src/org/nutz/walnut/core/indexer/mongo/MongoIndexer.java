package org.nutz.walnut.core.indexer.mongo;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.util.NutBean;
import org.nutz.mongo.ZMo;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.core.indexer.AbstractIoDataIndexer;
import org.nutz.walnut.impl.io.mongo.WnMongos;
import org.nutz.walnut.util.Wn;

import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoIndexer extends AbstractIoDataIndexer {

    private ZMoCo co;

    public MongoIndexer(WnObj root, MimeMap mimes, ZMoCo co) {
        super(root, mimes);
        this.co = co;
    }

    @Override
    protected WnObj _fetch_by_name(WnObj p, String name) {
        ZMoDoc q = ZMoDoc.NEW("pid", p.id()).putv("nm", name);
        ZMoDoc doc = co.findOne(q);
        WnIoObj obj = Mongos.toWnObj(doc);
        if (null != obj) {
            obj.setIndexer(this);
        }
        return obj;
    }

    private WnIoObj __get_by_full_id(String id) {
        ZMoDoc q = WnMongos.qID(id);
        ZMoDoc doc = co.findOne(q);
        WnIoObj obj = Mongos.toWnObj(doc);
        if (null != obj) {
            obj.setIndexer(this);
        }
        return obj;
    }

    @Override
    protected void _set(String id, NutBean map) {
        if (map.size() > 0) {
            ZMoDoc q = Mongos.qID(id);
            ZMoDoc doc = __map_to_doc_for_update(map);

            // 执行更新
            co.update(q, doc, true, false);
        }
    }

    private ZMoDoc __map_to_doc_for_update(NutBean map) {
        ZMoDoc doc = ZMoDoc.NEW();

        // 提炼字段
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            // 搞一下，否则 MongoDB 可能会挂
            if (val instanceof CharSequence) {
                val = val.toString();
            }
            boolean unset = key.startsWith("!");
            if (unset)
                key = key.substring(1);

            // ID 字段不能被修改
            if ("id".equals(key)) {
                continue;
            }
            // 如果为空，则表示 unset
            if (unset) {
                doc.unset(key);
            }
            // 其他的字段
            else {
                doc.set(key, val);
            }
        }
        return doc;
    }

    @Override
    protected WnIoObj _set_by(WnQuery q, NutBean map, boolean returnNew) {
        WnIoObj o = null;

        // 更新或者创建
        if (map.size() > 0) {
            ZMoDoc qDoc = Mongos.toQueryDoc(q);
            ZMoDoc update = __map_to_doc_for_update(map);
            ZMoDoc sort = ZMoDoc.NEW(q.sort());

            // 执行更新
            ZMoDoc doc = co.findAndModify(qDoc, null, sort, false, update, returnNew, false);

            // 执行结果
            if (null != doc) {
                o = Mongos.toWnObj(doc);
                o.setIndexer(this);
            }
        }

        // 返回
        return o;
    }

    @Override
    public int inc(WnQuery q, String key, int val, boolean returnNew) {
        ZMoDoc qDoc = Mongos.toQueryDoc(q);
        ZMoDoc fields = ZMoDoc.NEW(key, 1);
        ZMoDoc update = ZMoDoc.NEW().m("$inc", key, val);
        ZMoDoc sort = ZMoDoc.NEW(q.sort());

        ZMoDoc doc = co.findAndModify(qDoc, fields, sort, false, update, returnNew, false);

        return doc.getInt(key);
    }

    @Override
    public <T> T getAs(String id, String key, Class<T> classOfT, T dft) {
        ZMoDoc q = ZMoDoc.NEW("id", id);
        ZMoDoc flds = ZMoDoc.NEW(key, 1);
        ZMoDoc doc = co.findOne(q, flds);
        return doc.getAs(key, classOfT, dft);
    }

    @Override
    protected WnObj _create(WnIoObj o) {
        ZMoDoc doc = ZMo.me().toDoc(o).genID();
        // 一定不要记录 ph
        doc.removeField("ph");
        // 处理一下两段式 ID
        String myId = o.myId();
        doc.put("id", myId);
        // 保存
        co.save(doc);
        return o;
    }

    @Override
    public void delete(WnObj o) {
        co.remove(Mongos.qID(o.id()));
    }

    @Override
    protected WnIoObj _get_by_id(String id) {
        if (!Wn.isFullObjId(id)) {
            WnQuery q = new WnQuery().limit(2);
            q.setv("id", Pattern.compile("^" + id));
            q.limit(2);
            List<WnObj> objs = this.query(q);
            if (objs.isEmpty())
                return null;
            if (objs.size() > 1)
                throw Er.create("e.io.obj.get.shortid", id);
            return (WnIoObj) objs.get(0);
        }
        // 那就是完整的 ID 咯
        return this.__get_by_full_id(id);
    }

    @Override
    protected int _each(WnQuery q, Each<WnObj> callback) {
        // 木有必要迭代
        if (null == callback)
            return 0;

        // 准备查询
        ZMoDoc qDoc = null == q ? ZMoDoc.NEW() : Mongos.toQueryDoc(q);
        DBCursor cu = co.find(qDoc);

        try {
            cu.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
            int i = 0;
            int n = 0;
            int count = cu.count();
            Mongos.setup_paging(cu, q);
            Mongos.setup_sorting(cu, q);

            int limit = null == q ? 0 : q.limit();

            while (cu.hasNext()) {
                // 如果设置了分页 ...
                if (limit > 0 && n >= limit) {
                    break;
                }
                // 获取对象
                DBObject dbobj = cu.next();
                WnIoObj o = Mongos.toWnObj(dbobj);
                o.setIndexer(this);

                // 回调
                try {
                    callback.invoke(i++, o, count);
                    n++;
                }
                catch (ExitLoop e) {
                    break;
                }
                catch (ContinueLoop e) {}
            }

            return n;
        }
        finally {
            cu.close();
        }
    }

    // @Override
    // public long countChildren(WnObj o) {
    // if (o == null)
    // o = root.clone();
    //
    // // 否则，直接查询子
    // ZMoDoc qDoc = ZMoDoc.NEW("pid", o.myId());
    // return co.count(qDoc);
    // }

    @Override
    public long count(WnQuery q) {
        if (q == null)
            throw new RuntimeException("count without WnQuery is not allow");
        ZMoDoc qDoc = Mongos.toQueryDoc(q);
        if (qDoc.isEmpty())
            throw new RuntimeException("count with emtry WnQuery is not allow");

        // 对id的正则表达式进行更多的检查
        if (qDoc.containsField("id")) {
            Object tmp = qDoc.get("id");
            if (tmp != null && tmp instanceof Pattern && tmp.toString().equals("^")) {
                throw new RuntimeException("count with id:/^/ is not allow");
            }
        }

        return co.count(qDoc);
    }

    @Override
    public WnObj push(String id, String key, Object val, boolean returnNew) {
        ZMoDoc qDoc = ZMoDoc.NEW("id", id);
        ZMoDoc fields = ZMoDoc.NEW(key, 1);
        ZMoDoc update = ZMoDoc.NEW().m("$addToSet", key, val);

        ZMoDoc doc = co.findAndModify(qDoc, fields, null, false, update, returnNew, false);
        return Mongos.toWnObj(doc);
    }

    @Override
    public void push(WnQuery query, String key, Object val) {
        ZMoDoc qDoc = Mongos.toQueryDoc(query);
        ZMoDoc update = ZMoDoc.NEW().m("$addToSet", key, val);
        co.updateMulti(qDoc, update);
    }

    @Override
    public WnObj pull(String id, String key, Object val, boolean returnNew) {
        ZMoDoc qDoc = ZMoDoc.NEW("id", id);
        ZMoDoc fields = ZMoDoc.NEW(key, 1);
        ZMoDoc update = ZMoDoc.NEW().m("$pull", key, val);

        ZMoDoc doc = co.findAndModify(qDoc, fields, null, false, update, returnNew, false);

        return Mongos.toWnObj(doc);
    }

    @Override
    public void pull(WnQuery query, String key, Object val) {
        ZMoDoc qDoc = Mongos.toQueryDoc(query);
        ZMoDoc update = ZMoDoc.NEW().m("$pull", key, val);
        co.updateMulti(qDoc, update);
    }

}
