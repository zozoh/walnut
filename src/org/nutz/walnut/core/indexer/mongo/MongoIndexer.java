package org.nutz.walnut.core.indexer.mongo;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bson.Document;
import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mongo.ZMo;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.agg.WnAggGroupKey;
import org.nutz.walnut.api.io.agg.WnAggOptions;
import org.nutz.walnut.api.io.agg.WnAggOrderBy;
import org.nutz.walnut.api.io.agg.WnAggResult;
import org.nutz.walnut.api.io.agg.WnAggTransMode;
import org.nutz.walnut.api.io.agg.WnAggregateKey;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.core.indexer.AbstractIoDataIndexer;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Wtime;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;

public class MongoIndexer extends AbstractIoDataIndexer {

    private ZMoCo co;

    public MongoIndexer(WnObj root, MimeMap mimes, ZMoCo co) {
        super(root, mimes);
        this.co = co;
    }

    @SuppressWarnings("unchecked")
    @Override
    public WnAggResult aggregate(WnQuery q, WnAggOptions agg) {
        agg.assertValid();
        //
        // 准备聚集管线
        //
        List<ZMoDoc> aggPipline = new LinkedList<>();

        //
        // 准备聚集函数
        //
        String aggFunc;
        WnAggregateKey ak = agg.getAggregateBy();
        String akFrom = ak.getFromName();
        String akTo = ak.getToName();
        if (agg.isCOUNT()) {
            aggFunc = "{$sum:1}";
        }
        // 最大值
        else if (agg.isMAX()) {
            aggFunc = String.format("{$max:'$%s'}", akFrom);
        }
        // 最小值
        else if (agg.isMIN()) {
            aggFunc = String.format("{$min:'$%s'}", akFrom);
        }
        // 平均数
        else if (agg.isAVG()) {
            aggFunc = String.format("{$avg:'$%s'}", akFrom);
        }
        // 求和
        else if (agg.isSUM()) {
            aggFunc = String.format("{$sum:'$%s'}", akFrom);
        }
        // 不支持
        else {
            throw Er.create("e.io.agg.invalidAggFunc", agg.getFuncName());
        }

        //
        // 数据过滤
        //
        ZMoDoc match = Mongos.toQueryDoc(q);
        if (!match.isEmpty()) {
            aggPipline.add(ZMoDoc.NEW("$match", match));
        }

        //
        // 准备查询字段: $project
        //
        ZMoDoc project = ZMoDoc.NEWf("{_id:0,%s:1}", akFrom);
        for (WnAggGroupKey gk : agg.getGroupBy()) {
            String gkFrom = gk.getFromName();
            // 时间戳转日期
            if (WnAggTransMode.TIMESTAMP_TO_DATE == gk.getFunc()) {
                Date d1970 = Wtime.parseDate("1970-01-01T00:00:00");
                NutMap dscDate = Wlang.map("$add", Wlang.list(d1970, "$" + gkFrom));
                NutMap dsCovert = new NutMap();
                dsCovert.put("format", "%Y-%m-%d");
                dsCovert.put("date", dscDate);
                NutMap gkVal = Lang.map("$dateToString", dsCovert);
                project.put(gkFrom, gkVal);
            }
            // 普通字段
            else {
                project.put(gkFrom, 1);
            }
        }
        aggPipline.add(ZMoDoc.NEW("$project", project));

        //
        // 统计数据数量限制
        //
        if (agg.hasDataLimit()) {
            aggPipline.add(ZMoDoc.NEW("$limit", agg.getDataLimit()));
        }

        //
        // 聚集计算
        //
        NutMap group = Wlang.mapf("{%s:%s}", akTo, aggFunc);
        NutMap _grp_id = new NutMap();
        Map<String, WnAggGroupKey> gkToNameMap = new HashMap<>();
        for (WnAggGroupKey gk : agg.getGroupBy()) {
            String toName = gk.getToName();
            String fromName = gk.getFromName();
            _grp_id.put(toName, "$" + fromName);
            // 记录一下，排序的时候，好知道哪个键是 groupKey，以便在前面加 "_id."
            gkToNameMap.put(toName, gk);
        }
        group.put("_id", _grp_id);
        aggPipline.add(ZMoDoc.NEW("$group", group));

        //
        // 求和结果的排序
        //
        if (agg.hasOrderBy()) {
            NutMap sort = new NutMap();
            for (WnAggOrderBy ob : agg.getOrderBy()) {
                String obName = ob.getName();
                if (gkToNameMap.containsKey(obName)) {
                    obName = "_id." + obName;
                }
                sort.put(obName, ob.isAsc() ? 1 : -1);
            }
            aggPipline.add(ZMoDoc.NEW("$sort", sort));
        }

        //
        // 输出限制数量
        //
        if (agg.hasOutputLimit()) {
            aggPipline.add(ZMoDoc.NEW("$limit", agg.getOutputLimit()));
        }

        // 准备返回值
        WnAggResult re = new WnAggResult();

        // 建立游标开始查询
        AggregateIterable<Document> it = co.aggregate(aggPipline);
        MongoCursor<Document> cu = null;
        try {
            cu = it.cursor();
            while (cu.hasNext()) {
                Document dbobj = cu.next();
                Object _id = dbobj.get("_id");
                Object val = dbobj.get(akTo);

                NutBean bean = new NutMap();
                bean.put(akTo, val);

                if (_id instanceof Map) {
                    bean.putAll((Map<String, Object>) _id);
                }

                re.add(bean);
            }
        }
        finally {
            Streams.safeClose(cu);
        }

        // 搞定
        return re;
    }

    @Override
    protected WnObj _fetch_by_name(WnObj p, String name) {
        ZMoDoc q = ZMoDoc.NEW("pid", p.id()).putv("nm", name);
        Document dbobj = co.find(q).first();
        ZMoDoc doc = ZMoDoc.WRAP(dbobj);
        WnIoObj obj = Mongos.toWnObj(doc);
        if (null != obj) {
            obj.setIndexer(this);
        }
        return obj;
    }

    private WnIoObj __get_by_full_id(String id) {
        ZMoDoc q = Mongos.qID(id);
        Document dbobj = co.find(q).first();
        ZMoDoc doc = ZMoDoc.WRAP(dbobj);
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
            co.updateOne(q, doc);
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

            FindOneAndUpdateOptions opt = new FindOneAndUpdateOptions();
            if (returnNew) {
                opt.returnDocument(ReturnDocument.AFTER);
            } else {
                opt.returnDocument(ReturnDocument.BEFORE);
            }

            // 执行更新
            Document dbobj = co.findOneAndUpdate(qDoc, update, opt);
            ZMoDoc doc = ZMoDoc.WRAP(dbobj);

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
        ZMoDoc update = ZMoDoc.NEW().m("$inc", key, val);

        FindOneAndUpdateOptions opt = new FindOneAndUpdateOptions();
        if (returnNew) {
            opt.returnDocument(ReturnDocument.AFTER);
        } else {
            opt.returnDocument(ReturnDocument.BEFORE);
        }

        Document dbobj = co.findOneAndUpdate(qDoc, update, opt);
        ZMoDoc doc = ZMoDoc.WRAP(dbobj);

        return doc.getInt(key);
    }

    @Override
    public <T> T getAs(String id, String key, Class<T> classOfT, T dft) {
        ZMoDoc q = ZMoDoc.NEW("id", id);

        Document dbobj = co.find(q).first();
        ZMoDoc doc = ZMoDoc.WRAP(dbobj);

        return doc.getAs(key, classOfT, dft);
    }

    @Override
    protected WnObj _create(WnIoObj o) {
        ZMoDoc doc = ZMo.me().toDoc(o);
        // 一定不要记录 ph
        doc.remove("ph");
        // 处理一下两段式 ID
        String myId = o.myId();
        doc.put("id", myId);
        // 保存
        co.insertOne(doc);
        return o;
    }

    @Override
    public void delete(WnObj o) {
        ZMoDoc q = Mongos.qID(o.id());
        co.deleteOne(q);
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
        FindIterable<Document> it = co.find(qDoc);
        MongoCursor<Document> cu = null;
        try {
            // cu.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
            int i = 0;
            int n = 0;
            Mongos.setup_paging(it, q);
            Mongos.setup_sorting(it, q);

            int limit = null == q ? 0 : q.limit();

            cu = it.cursor();
            while (cu.hasNext()) {
                // 如果设置了分页 ...
                if (limit > 0 && n >= limit) {
                    break;
                }
                // 获取对象
                Document dbobj = cu.next();
                WnIoObj o = Mongos.toWnObj(dbobj);
                o.setIndexer(this);

                // 回调
                try {
                    callback.invoke(i++, o, -1);
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
            Streams.safeClose(cu);
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
        if (qDoc.containsKey("id")) {
            Object tmp = qDoc.get("id");
            if (tmp != null && tmp instanceof Pattern && tmp.toString().equals("^")) {
                throw new RuntimeException("count with id:/^/ is not allow");
            }
        }

        return co.countDocuments(qDoc);
    }

    @Override
    public WnObj push(String id, String key, Object val, boolean returnNew) {
        ZMoDoc qDoc = ZMoDoc.NEW("id", id);
        ZMoDoc update = ZMoDoc.NEW().m("$addToSet", key, val);
        FindOneAndUpdateOptions opt = new FindOneAndUpdateOptions();
        if (returnNew) {
            opt.returnDocument(ReturnDocument.AFTER);
        } else {
            opt.returnDocument(ReturnDocument.BEFORE);
        }
        Document dbobj = co.findOneAndUpdate(qDoc, update, opt);
        ZMoDoc doc = ZMoDoc.WRAP(dbobj);
        return Mongos.toWnObj(doc);
    }

    @Override
    public void push(WnQuery query, String key, Object val) {
        ZMoDoc qDoc = Mongos.toQueryDoc(query);
        ZMoDoc update = ZMoDoc.NEW().m("$addToSet", key, val);
        co.updateMany(qDoc, update);
    }

    @Override
    public WnObj pull(String id, String key, Object val, boolean returnNew) {
        ZMoDoc qDoc = ZMoDoc.NEW("id", id);
        ZMoDoc update = ZMoDoc.NEW().m("$pull", key, val);

        FindOneAndUpdateOptions opt = new FindOneAndUpdateOptions();
        if (returnNew) {
            opt.returnDocument(ReturnDocument.AFTER);
        } else {
            opt.returnDocument(ReturnDocument.BEFORE);
        }

        Document dbobj = co.findOneAndUpdate(qDoc, update, opt);
        ZMoDoc doc = ZMoDoc.WRAP(dbobj);

        return Mongos.toWnObj(doc);
    }

    @Override
    public void pull(WnQuery query, String key, Object val) {
        ZMoDoc qDoc = Mongos.toQueryDoc(query);
        ZMoDoc update = ZMoDoc.NEW().m("$pull", key, val);
        co.updateMany(qDoc, update);
    }

}
