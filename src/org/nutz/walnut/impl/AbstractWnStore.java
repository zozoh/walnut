package org.nutz.walnut.impl;

import java.io.InputStream;

import org.nutz.lang.Each;
import org.nutz.lang.stream.NullInputStream;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnStore;
import org.nutz.walnut.api.io.WnStoreTable;

public abstract class AbstractWnStore implements WnStore {

    protected WnStoreTable table;

    public AbstractWnStore(WnStoreTable table) {
        this.table = table;
    }

    @Override
    public void _clean_for_unit_test() {
        table._clean_for_unit_test();
    }

    @Override
    public InputStream getInputStream(WnObj o, long off) {
        // TODO zozoh: 这个应该弄的复杂点，如果 InputStream 读到尾部了，还能继续读才对
        WnHistory his = table.getHistory(o, 0);
        if (null == his)
            return new NullInputStream();
        return getInputStream(his, off);
    }

    public int eachHistory(WnObj o, long nano, Each<WnHistory> callback) {
        return table.eachHistory(o, nano, callback);
    }

    public WnHistory getHistory(WnObj o, long nano) {
        return table.getHistory(o, nano);
    }

    public WnHistory addHistory(String oid, String data, String sha1, long len) {
        return table.addHistory(oid, data, sha1, len);
    }

    public int cleanHistory(WnObj o, long nano) {
        return table.cleanHistory(o, nano);
    }

    public int cleanHistoryBy(WnObj o, int remain) {
        return table.cleanHistoryBy(o, remain);
    }

}
