package org.nutz.walnut.impl.io.mongo;

import java.util.Map;
import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.util.NutMap;
import org.nutz.mongo.ZMo;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.io.AbstractWnTree;
import org.nutz.walnut.util.Wn;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoWnTree extends AbstractWnTree {

    private ZMoCo co;

    public MongoWnTree(ZMoCo co) {
        this.co = co;
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
        ZMoDoc doc = co.findOne(ZMoDoc.NEW("pid", nd.id()));
        return null != doc;
    }

    @Override
    public int each(WnQuery q, Each<WnObj> callback) {
        if (null == callback)
            return 0;
        ZMoDoc qDoc = null == q ? ZMoDoc.NEW() : WnMongos.toQueryDoc(q);
        DBCursor cu = co.find(qDoc);

        try {
            int i = 0;
            int n = 0;
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
                WnObj o = WnMongos.toWnObj(dbobj);
                try {
                    callback.invoke(i++, o, n);
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
        ZMoDoc qDoc = null == q ? ZMoDoc.NEW() : WnMongos.toQueryDoc(q);
        return co.count(qDoc);
    }

    @Override
    protected void _set(String id, NutMap map) {
        // 更新或者创建
        if (map.size() > 0) {
            ZMoDoc q = WnMongos.qID(id);
            ZMoDoc doc = ZMoDoc.NEW();

            // 提炼字段
            for (Map.Entry<String, Object> en : map.entrySet()) {
                String key = en.getKey();
                // ID 字段不能被修改
                if ("id".equals(key))
                    continue;
                // 其他的字段
                doc.set(key, en.getValue());
            }

            // 执行更新
            co.update(q, doc, true, false);
        }
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

    @Override
    protected WnObj _do_append(WnObj p, WnObj nd, String newName) {
        // 开始移动
        ZMoDoc q = WnMongos.qID(nd.id());
        ZMoDoc doc = ZMoDoc.SET("pid", p.id()).set("nm", newName);
        co.update(q, doc);

        // 更新内存
        nd.path(Wn.appendPath(p.path(), newName));
        nd.name(newName);
        nd.setParent(p);

        // 返回
        return nd;
    }

}
