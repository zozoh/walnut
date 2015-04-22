package org.nutz.walnut.impl;

import java.io.InputStream;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.stream.NullInputStream;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnStore;
import org.nutz.walnut.api.io.WnStoreTable;
import org.nutz.walnut.util.Wn;

public abstract class AbstractWnStore implements WnStore {

    protected WnStoreTable table;

    protected WnIndexer indexer;

    public AbstractWnStore(WnIndexer indexer, WnStoreTable table) {
        this.indexer = indexer;
        this.table = table;
    }

    @Override
    public void _clean_for_unit_test() {
        table._clean_for_unit_test();
    }

    @Override
    public InputStream getInputStream(WnObj o, long off) {
        // TODO zozoh: 这个应该弄的复杂点，如果 InputStream 读到尾部了，还能继续读才对
        WnHistory his = table.getHistory(o, o.nanoStamp());
        if (null == his)
            return new NullInputStream();
        return getInputStream(his, off);
    }

    public int eachHistory(WnObj o, long nano, Each<WnHistory> callback) {
        return table.eachHistory(o, nano, callback);
    }

    @Override
    public List<WnHistory> getHistoryList(WnObj o, long nano) {
        return table.getHistoryList(o, nano);
    }

    public WnHistory getHistory(WnObj o, long nano) {
        return table.getHistory(o, nano);
    }

    public WnHistory addHistory(String oid, String data, String sha1, long len) {
        return table.addHistory(oid, data, sha1, len);
    }

    public List<WnHistory> cleanHistory(WnObj o, long nano) {
        List<WnHistory> list = table.cleanHistory(o, nano);
        return _do_remove_list(o, list);
    }

    private List<WnHistory> _do_remove_list(WnObj o, List<WnHistory> list) {
        boolean should_clean_obj_index = false;
        for (WnHistory his : list) {
            do_real_remove_history_data(his);

            // 如果是对象最后一条记录，则表示清空对象内容
            if (his.nanoStamp() == o.nanoStamp()
                && (his.isSameSha1(o.sha1()) || his.isSameData(o.data()))) {
                should_clean_obj_index = true;
            }
        }

        if (should_clean_obj_index) {
            o.data(null);
            o.sha1(null);
            o.len(0);
            o.mender(Wn.WC().checkMe());
            o.nanoStamp(System.nanoTime());
            indexer.set(o.id(), o.toMap4Update("^m|sha1|data|len|lm|nano$"));
        }

        return list;
    }

    public List<WnHistory> cleanHistoryBy(WnObj o, int remain) {
        List<WnHistory> list = table.cleanHistoryBy(o, remain);
        return _do_remove_list(o, list);
    }

    protected abstract void do_real_remove_history_data(WnHistory his);
}
