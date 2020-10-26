package org.nutz.walnut.core.refer.mongo;

import java.util.HashSet;
import java.util.Set;

import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.core.WnReferApi;
import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoReferService implements WnReferApi {

    private ZMoCo co;

    public MongoReferService(ZMoCo co) {
        this.co = co;
    }

    @Override
    public long add(String targetId, String... referIds) {
        ZMoDoc q;
        for (String referId : referIds) {
            // 查一下: 存在就无视
            q = ZMoDoc.NEW("tid", targetId).putv("rid", referId);
            if (co.count(q) > 0) {
                continue;
            }

            // 不存在就添加
            co.save(q);
        }
        q = ZMoDoc.NEW("tid", targetId);
        return co.count(q);
    }

    @Override
    public long remove(String targetId, String... referIds) {
        ZMoDoc q;
        // 逐个删除
        for (String referId : referIds) {
            q = ZMoDoc.NEW("tid", targetId).putv("rid", referId);
            co.remove(q);
        }
        q = ZMoDoc.NEW("tid", targetId);
        return co.count(q);
    }

    @Override
    public long count(String targetId) {
        ZMoDoc q = ZMoDoc.NEW("tid", targetId);
        return co.count(q);
    }

    @Override
    public Set<String> all(String targetId) {
        HashSet<String> sets = new HashSet<>();
        ZMoDoc q = ZMoDoc.NEW("tid", targetId);

        DBCursor cu = co.find(q);

        try {
            cu.addOption(Bytes.QUERYOPTION_NOTIMEOUT);

            while (cu.hasNext()) {
                // 获取对象引用 ID
                DBObject dbobj = cu.next();
                Object rid = dbobj.get("rid");

                if (null != rid) {
                    sets.add(rid.toString());
                }
            }
        }
        finally {
            cu.close();
        }

        return sets;
    }

    @Override
    public void clear(String targetId) {
        ZMoDoc q = ZMoDoc.NEW("tid", targetId);
        co.remove(q);
    }

}
