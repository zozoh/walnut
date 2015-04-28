package org.nutz.walnut.impl.mongo;

import java.util.Map;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.util.NutMap;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.AbstractWnIndexer;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoWnIndexer extends AbstractWnIndexer {

    private ZMoCo co;

    public MongoWnIndexer(ZMoCo co) {
        this.co = co;
    }

    @Override
    public void _clean_for_unit_test() {
        co.remove(ZMoDoc.NEW());
    }

    @Override
    public WnObj _get(String id) {
        ZMoDoc q = WnMongos.qID(id);
        ZMoDoc doc = co.findOne(q);
        return WnMongos.toWnObj(doc);
    }

    @Override
    public void _set(String id, NutMap map) {
        // 移除
        if (null == map) {
            remove(id);
        }
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
    public int _each(WnQuery q, Each<WnObj> callback) {
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
    public void _remove(WnObj o) {
        co.remove(WnMongos.qID(o.id()));
    }

}
