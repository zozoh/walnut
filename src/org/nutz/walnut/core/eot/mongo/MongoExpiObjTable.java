package org.nutz.walnut.core.eot.mongo;

import java.util.ArrayList;
import java.util.List;

import org.nutz.log.Log;
import org.nutz.walnut.util.Wlog;
import org.nutz.mongo.ZMo;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.api.io.WnExpiObj;
import org.nutz.walnut.api.io.WnExpiObjTable;
import org.nutz.walnut.util.Wn;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

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
        co.update(q, doc, true, false);
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
        WriteResult wr = co.remove(q);
        return wr.getN() > 0;
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
        DBCursor cu = co.find(q);
        cu.sort(ZMoDoc.NEW("expi", 1));
        // cu.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
        int count = cu.count();
        cu.limit(limit);

        // 遍历数据
        List<WnExpiObj> list = new ArrayList<>(count);
        try {
            while (cu.hasNext()) {
                DBObject dbobj = cu.next();
                MoExpiObj o = ZMo.me().fromDocToObj(dbobj, MoExpiObj.class);
                o.setHoldTime(myHold);
                list.add(o);

                // 占用住
                ZMoDoc o_q = ZMoDoc.ID(dbobj.get("_id"));
                co.update(o_q, o_u, true, false);
            }
        }
        catch (Exception e) {
            log.warn("Something wrong when takeover", e);
        }
        finally {
            cu.close();
        }

        // 搞定
        return list;
    }

    @Override
    public int clean(String owner, long hold) {
        ZMoDoc q = ZMoDoc.NEW("hold", hold);
        q.put("ow", owner);
        WriteResult wr = co.remove(q);
        return wr.getN();
    }

}
