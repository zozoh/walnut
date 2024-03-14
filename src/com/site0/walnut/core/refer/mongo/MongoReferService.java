package com.site0.walnut.core.refer.mongo;

import java.util.HashSet;
import java.util.Set;

import org.bson.Document;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import com.site0.walnut.core.WnReferApi;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;

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
            long n = co.countDocuments(q);
            if (n > 0) {
                continue;
            }

            // 不存在就添加
            co.insertOne(q);
        }
        q = ZMoDoc.NEW("tid", targetId);
        return co.countDocuments(q);
    }

    @Override
    public long remove(String targetId, String... referIds) {
        ZMoDoc q;
        // 逐个删除
        for (String referId : referIds) {
            q = ZMoDoc.NEW("tid", targetId).putv("rid", referId);
            co.deleteMany(q);
        }
        q = ZMoDoc.NEW("tid", targetId);
        return co.countDocuments(q);
    }

    @Override
    public long count(String targetId) {
        ZMoDoc q = ZMoDoc.NEW("tid", targetId);
        return co.countDocuments(q);
    }

    @Override
    public Set<String> all(String targetId) {
        HashSet<String> sets = new HashSet<>();
        ZMoDoc q = ZMoDoc.NEW("tid", targetId);

        FindIterable<Document> it = co.find(q);
        MongoCursor<Document> cu = null;

        try {
            // cu.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
            cu = it.cursor();
            while (cu.hasNext()) {
                // 获取对象引用 ID
                Document dbobj = cu.next();
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
        co.deleteMany(q);
    }

}
