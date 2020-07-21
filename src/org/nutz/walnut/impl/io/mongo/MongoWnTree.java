package org.nutz.walnut.impl.io.mongo;

import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.util.NutMap;
import org.nutz.mongo.ZMo;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.io.AbstractWnTree;
import org.nutz.walnut.impl.io.WnBean;

import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoWnTree extends AbstractWnTree {

    private ZMoCo co;

    public MongoWnTree(ZMoCo co, WnObj root, MimeMap mimes) {
        super();
        this.co = co;
        this.setRoot(root);
        this.mimes = mimes;
    }

    @Override
    public void _clean_for_unit_test() {
        co.remove(ZMoDoc.NEW());
    }

    @Override
    protected WnObj _get_my_node(String id) {
        ZMoDoc q = WnMongos.qID(id);
        ZMoDoc doc = co.findOne(q);
        if (null != doc) {
            return WnMongos.toWnObj(doc);
        }
        return null;
    }

    @Override
    protected WnObj _fetch_one_by_name(WnObj p, String name) {
        ZMoDoc q = ZMoDoc.NEW("pid", p.id()).putv("nm", name);
        ZMoDoc doc = co.findOne(q);
        return WnMongos.toWnObj(doc);
    }

    @Override
    public boolean exists(WnObj p, String name) {
        ZMoDoc q = ZMoDoc.NEW("pid", p.id()).putv("nm", name);
        return co.count(q) > 0;
    }

    @Override
    public boolean hasChild(WnObj nd) {
        return co.count(ZMoDoc.NEW("pid", nd.id())) != 0;
    }

    @Override
    protected int _each(WnQuery q, Each<WnObj> callback) {
        if (null == callback)
            return 0;
        ZMoDoc qDoc = null == q ? ZMoDoc.NEW() : WnMongos.toQueryDoc(q);
        DBCursor cu = co.find(qDoc);

        try {
            cu.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
            int i = 0;
            int n = 0;
            int count = cu.count();
            WnMongos.setup_paging(cu, q);
            WnMongos.setup_sorting(cu, q);

            int limit = null == q ? 0 : q.limit();

            while (cu.hasNext()) {
                // 如果设置了分页 ...
                if (limit > 0 && n >= limit) {
                    break;
                }
                // 获取对象
                DBObject dbobj = cu.next();
                WnBean o = WnMongos.toWnObj(dbobj);
                o.setTree(this);
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

    @Override
    public long count(WnQuery q) {
        if (q == null)
            throw new RuntimeException("count without WnQuery is not allow");
        ZMoDoc qDoc = WnMongos.toQueryDoc(q);
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
    protected void _set(String id, NutMap map) {
        // 更新或者创建
        if (map.size() > 0) {
            ZMoDoc q = WnMongos.qID(id);
            ZMoDoc doc = __map_to_doc_for_update(map);

            // 执行更新
            co.update(q, doc, true, false);
        }
    }

    @Override
    protected WnObj _set_by(WnQuery q, NutMap map, boolean returnNew) {
        WnObj o = null;

        // 必须得有条件
        if (null == q || q.isEmptyMatch()) {
            return null;
        }

        // 更新或者创建
        if (map.size() > 0) {
            ZMoDoc qDoc = WnMongos.toQueryDoc(q);
            ZMoDoc update = __map_to_doc_for_update(map);
            ZMoDoc sort = ZMoDoc.NEW(q.sort());

            // 执行更新
            ZMoDoc doc = co.findAndModify(qDoc, null, sort, false, update, returnNew, false);

            // 执行结果
            if (null != doc)
                o = WnMongos.toWnObj(doc);
        }

        // 返回
        return o;
    }

    private ZMoDoc __map_to_doc_for_update(NutMap map) {
        ZMoDoc doc = ZMoDoc.NEW();

        // 提炼字段
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
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
    public int inc(WnQuery q, String key, int val, boolean returnNew) {
        ZMoDoc qDoc = WnMongos.toQueryDoc(q);
        ZMoDoc fields = ZMoDoc.NEW(key, 1);
        ZMoDoc update = ZMoDoc.NEW().m("$inc", key, val);
        ZMoDoc sort = ZMoDoc.NEW(q.sort());

        ZMoDoc doc = co.findAndModify(qDoc, fields, sort, false, update, returnNew, false);

        return doc.getInt(key);
    }

    @Override
    public int getInt(String id, String key, int dft) {
        ZMoDoc q = ZMoDoc.NEW("id", id);
        ZMoDoc flds = ZMoDoc.NEW(key, 1);
        ZMoDoc doc = co.findOne(q, flds);
        return doc.getInt(key, dft);
    }

    @Override
    public long getLong(String id, String key, long dft) {
        ZMoDoc q = ZMoDoc.NEW("id", id);
        ZMoDoc flds = ZMoDoc.NEW(key, 1);
        ZMoDoc doc = co.findOne(q, flds);
        return doc.getLong(key, dft);
    }

    @Override
    public String getString(String id, String key, String dft) {
        ZMoDoc q = ZMoDoc.NEW("id", id);
        ZMoDoc flds = ZMoDoc.NEW(key, 1);
        ZMoDoc doc = co.findOne(q, flds);
        return doc.getString(key, dft);
    }

    @Override
    public <T> T getAs(String id, String key, Class<T> classOfT, T dft) {
        ZMoDoc q = ZMoDoc.NEW("id", id);
        ZMoDoc flds = ZMoDoc.NEW(key, 1);
        ZMoDoc doc = co.findOne(q, flds);
        return doc.getAs(key, classOfT, dft);
    }

    @Override
    protected void _create_node(WnObj o) {
        ZMoDoc doc = ZMo.me().toDoc(o).genID();
        doc.removeField("ph");
        co.save(doc);
    }

    @Override
    protected void _delete_self(WnObj nd) {
        co.remove(WnMongos.qID(nd.id()));
    }

    // @Override
    // public WnObj getDirect(String id) {
    // return get(id);
    // }

    @Override
    public WnObj push(String id, String key, Object val, boolean returnNew) {
        ZMoDoc qDoc = ZMoDoc.NEW("id", id);
        ZMoDoc fields = ZMoDoc.NEW(key, 1);
        ZMoDoc update = ZMoDoc.NEW().m("$addToSet", key, val);

        ZMoDoc doc = co.findAndModify(qDoc, fields, null, false, update, returnNew, false);

        return WnMongos.toWnObj(doc);
    }

    @Override
    public void push(WnQuery query, String key, Object val) {
        ZMoDoc qDoc = WnMongos.toQueryDoc(query);
        ZMoDoc update = ZMoDoc.NEW().m("$addToSet", key, val);
        co.updateMulti(qDoc, update);
    }

    @Override
    public WnObj pull(String id, String key, Object val, boolean returnNew) {
        ZMoDoc qDoc = ZMoDoc.NEW("id", id);
        ZMoDoc fields = ZMoDoc.NEW(key, 1);
        ZMoDoc update = ZMoDoc.NEW().m("$pull", key, val);

        ZMoDoc doc = co.findAndModify(qDoc, fields, null, false, update, returnNew, false);

        return WnMongos.toWnObj(doc);
    }

    @Override
    public void pull(WnQuery query, String key, Object val) {
        ZMoDoc qDoc = WnMongos.toQueryDoc(query);
        ZMoDoc update = ZMoDoc.NEW().m("$pull", key, val);
        co.updateMulti(qDoc, update);
    }

}
