package org.nutz.walnut.impl.io;

import java.io.InputStream;
import java.io.OutputStream;
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
    public String getRealPath(WnObj o) {
        // TODO 模仿getInputStream
        WnHistory his = table.getHistory(o, o.nanoStamp());
        if (null == his)
            return null;
        return _get_realpath(his);
    }

    protected abstract String _get_realpath(WnHistory his);

    @Override
    public InputStream getInputStream(WnObj o, long off) {
        // TODO zozoh: 这个应该弄的复杂点，如果 InputStream 读到尾部了，还能继续读才对
        WnHistory his = table.getHistory(o, o.nanoStamp());
        if (null == his)
            return new NullInputStream();
        return getInputStream(o, his, off);
    }

    @Override
    public InputStream getInputStream(WnObj o, WnHistory his, long off) {
        if (null == his)
            return new NullInputStream();

        // 返回输出流
        return _get_inputstream(his, off);
    }

    protected abstract InputStream _get_inputstream(WnHistory his, long off);

    @Override
    public OutputStream getOutputStream(WnObj o, long off) {
        // 返回输出流
        return _get_outputstream(o, off);
    }

    protected abstract OutputStream _get_outputstream(WnObj o, long off);

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
            _do_real_remove_history_data(his);

            // 如果是对象最后一条记录，则表示清空对象内容
            if (his.nanoStamp() == o.nanoStamp()
                && (his.isSameSha1(o.sha1()) || his.isSameData(o.data()))) {
                should_clean_obj_index = true;
            }
        }

        if (should_clean_obj_index) {
            // 特殊操作将禁止更新 index
            if (!Wn.WC().getBoolean("store:clean_not_update_indext")) {
                o.data(null);
                o.sha1(null);
                o.len(0);
                o.mender(Wn.WC().checkMe());
                o.nanoStamp(Wn.nanoTime());
                indexer.set(o, "^m|sha1|data|len|lm|nano$");
            }
        }

        return list;
    }

    public List<WnHistory> cleanHistoryBy(WnObj o, int remain) {
        List<WnHistory> list = table.cleanHistoryBy(o, remain);
        return _do_remove_list(o, list);
    }

    protected abstract void _do_real_remove_history_data(WnHistory his);
}
