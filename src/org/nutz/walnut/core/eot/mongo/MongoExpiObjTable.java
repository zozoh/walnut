package org.nutz.walnut.core.eot.mongo;

import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import org.nutz.lang.Streams;
import org.nutz.log.Log;
import org.nutz.walnut.util.Wlog;
import org.nutz.mongo.ZMo;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.api.io.WnExpiObj;
import org.nutz.walnut.api.io.WnExpiObjTable;
import org.nutz.walnut.util.Wn;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;

public class MongoExpiObjTable implements WnExpiObjTable {

    private static final Log log = Wlog.getIO();

    private ZMoCo co;

    public MongoExpiObjTable(ZMoCo co) {
        this.co = co;
    }

    @Override
    public void insertOrUpdate(WnExpiObj o) {
        ZMoDoc q = ZMoDoc.NEW("id", o.getId());
        ZMoDoc doc = ZMo.me().toDoc(o);
        ZMoDoc update = ZMoDoc.SET(doc);
        UpdateOptions uo = new UpdateOptions().upsert(true);
        co.updateOne(q, update, uo);
    }

    @Override
    public void insertOrUpdate(String id, long expi) {
        MoExpiObj eo = new MoExpiObj();
        eo.setId(id);
        eo.setExpiTime(expi);
        this.insertOrUpdate(eo);
    }

    @Override
    public boolean remove(String id) {
        ZMoDoc q = ZMoDoc.NEW("id", id);
        DeleteResult dr = co.deleteMany(q);
        return dr.getDeletedCount() > 0;
    }

    @Override
    public List<WnExpiObj> takeover(String owner, long duInMs, int limit) {
        // 首先查询出来已经过期的对象
        long now = Wn.now();
        ZMoDoc q = ZMoDoc.NEW().lt("expi", now);
        q.lt("hold", now);

        // 我持有对象的时间
        long myHold = now + duInMs;
        ZMoDoc o_u = ZMoDoc.NEW();
        o_u.set("hold", myHold).set("ow", owner);

        // 必须要有个限制
        if (limit <= 0) {
            limit = 100;
        }

        // 设置游标
        FindIterable<Document> it = co.find(q);
        it.sort(ZMoDoc.NEW("expi", 1));
        it.limit(limit);

        MongoCursor<Document> cu = null;
        // cu.addOption(Bytes.QUERYOPTION_NOTIMEOUT);

        // 遍历数据
        List<WnExpiObj> list = new LinkedList<>();
        try {
            cu = it.cursor();
            while (cu.hasNext()) {
                Document dbobj = cu.next();
                ZMoDoc doc = ZMoDoc.WRAP(dbobj);
                MoExpiObj o = ZMo.me().fromDocToObj(doc, MoExpiObj.class);
                o.setHoldTime(myHold);
                list.add(o);

                // 占用住
                ZMoDoc o_q = ZMoDoc.ID(dbobj.get("_id"));
                co.updateOne(o_q, o_u);
            }
        }
        catch (Exception e) {
            log.warn("Something wrong when takeover", e);
        }
        finally {
            Streams.safeClose(cu);
        }

        // 搞定
        return list;
    }

    @Override
    public int clean(String owner, long hold) {
        ZMoDoc q = ZMoDoc.NEW("hold", hold);
        q.put("ow", owner);
        DeleteResult dr = co.deleteMany(q);
        if (dr.wasAcknowledged()) {
            return (int) dr.getDeletedCount();
        }
        return -1;
    }

}
