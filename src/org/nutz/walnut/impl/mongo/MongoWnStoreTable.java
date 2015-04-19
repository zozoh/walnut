package org.nutz.walnut.impl.mongo;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.Lang;
import org.nutz.mongo.ZMo;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnStoreTable;
import org.nutz.walnut.util.Wn;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

public class MongoWnStoreTable implements WnStoreTable {

    private ZMoCo co;

    public MongoWnStoreTable(ZMoCo co) {
        this.co = co;
    }

    @Override
    public int eachHistory(WnObj o, long nano, Each<WnHistory> callback) {
        ZMoDoc q = ZMoDoc.NEW("oid", o.id());
        if (nano > 0) {
            q.lte("nano", nano);
        }
        DBCursor cu = co.find(q).sort(ZMoDoc.NEW("nano", -1));

        try {
            int i = 0;
            int n = 0;

            while (cu.hasNext()) {
                DBObject dbobj = cu.next();
                WnHistory his = WnMongos.toWnHistory(dbobj);

                // 调用回调并计数
                try {
                    callback.invoke(i++, his, n);
                }
                catch (ExitLoop e) {
                    break;
                }
                catch (ContinueLoop e) {}
                finally {
                    n++;
                }
            }

            return n;
        }
        finally {
            cu.close();
        }
    }

    @Override
    public WnHistory getHistory(WnObj o, long nano) {
        final WnHistory[] his = new WnHistory[1];
        eachHistory(o, nano, new Each<WnHistory>() {
            public void invoke(int index, WnHistory ele, int length) {
                his[0] = ele;
                Lang.Break();
            }
        });
        return his[0];
    }

    @Override
    public WnHistory addHistory(String oid, String data, String sha1, long len) {
        WnHistory his = new MongoWnHistory();
        his.oid(oid).data(data).sha1(sha1).len(len);
        his.nanoStamp(System.nanoTime());
        his.owner(Wn.WC().checkMe());

        ZMoDoc doc = ZMo.me().toDoc(his);
        co.save(doc);

        return his;
    }

    @Override
    public int cleanHistory(WnObj o, long nano) {
        ZMoDoc q = ZMoDoc.NEW("oid", o.id());
        if (nano > 0) {
            q.lt("nano", nano);
        }
        WriteResult wr = co.remove(q);
        return wr.getN();
    }

    @Override
    public int cleanHistoryBy(WnObj o, int remain) {
        // 找到最后一个保留记录的 nano 时间
        // 如果 remain 小于1，则表示删除除了最后一条记录的所有记录
        long nano = o.nanoStamp();
        if (remain > 1) {
            ZMoDoc q = ZMoDoc.NEW("oid", o.id());
            DBCursor cu = co.find(q, ZMoDoc.NEW("nano", 1))
                            .sort(ZMoDoc.NEW("nano", -1))
                            .skip(remain - 1);
            try {
                while (cu.hasNext()) {
                    DBObject dbobj = cu.next();
                    nano = (Long) dbobj.get("nano");
                }
            }
            finally {
                cu.close();
            }
        }

        // 删除这个 nano 时间之后的记录
        return cleanHistory(o, nano);
    }

    @Override
    public void _clean_for_unit_test() {
        co.remove(ZMoDoc.NEW());
    }
}
