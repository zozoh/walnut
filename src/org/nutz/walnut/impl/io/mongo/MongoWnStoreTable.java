package org.nutz.walnut.impl.io.mongo;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.mongo.ZMo;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.io.AbstractWnStoreTable;
import org.nutz.walnut.util.Wn;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoWnStoreTable extends AbstractWnStoreTable {

    private ZMoCo co;

    public MongoWnStoreTable(ZMoCo co) {
        this.co = co;
    }

    @Override
    public int eachHistory(WnObj o, long nano, Each<WnHistory> callback) {
        ZMoDoc q = ZMoDoc.NEW("oid", o.id());
        if (nano >= 0) {
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
    public WnHistory addHistory(String oid, String data, String sha1, long len) {
        WnHistory his = new MongoWnHistory();
        his.oid(oid).data(data).sha1(sha1).len(len);
        his.nanoStamp(Wn.nanoTime());
        his.owner(Wn.WC().checkMe());

        ZMoDoc doc = ZMo.me().toDoc(his);
        co.save(doc);

        return his;
    }

    @Override
    public List<WnHistory> cleanHistory(WnObj o, long nano) {
        final List<WnHistory> list = new LinkedList<WnHistory>();
        this.eachHistory(o, nano, new Each<WnHistory>() {
            public void invoke(int index, WnHistory his, int length) {
                // 删除索引记录
                ZMoDoc q = ZMoDoc.NEWf("oid:'%s',sha1:'%s',nano:%d",
                                       his.oid(),
                                       his.sha1(),
                                       his.nanoStamp());
                co.remove(q);

                // 如果引用的 SHA1 不在有别的记录使用，加入记录
                q = ZMoDoc.NEW("sha1", his.sha1());
                DBCursor cu = co.find(q);
                try {
                    if (!cu.hasNext()) {
                        list.add(his);
                    }
                }
                finally {
                    cu.close();
                }
            }
        });
        return list;
    }

    @Override
    public List<WnHistory> cleanHistoryBy(WnObj o, int remain) {
        // 全都删掉
        if (remain <= 0)
            return cleanHistory(o, -1);

        // 找到最后一个保留记录的 nano 时间
        // 如果 remain == 0，则表示删除对象所有的历史记录
        long nano = o.nanoStamp();
        ZMoDoc q = ZMoDoc.NEW("oid", o.id());
        DBCursor cu = co.find(q, ZMoDoc.NEW("nano", 1)).sort(ZMoDoc.NEW("nano", -1)).skip(remain);
        try {
            while (cu.hasNext()) {
                DBObject dbobj = cu.next();
                nano = (Long) dbobj.get("nano");
            }
        }
        finally {
            cu.close();
        }

        // 删除这个 nano 时间之后的记录
        return cleanHistory(o, nano);
    }

    @Override
    public void _clean_for_unit_test() {
        co.remove(ZMoDoc.NEW());
    }
}
