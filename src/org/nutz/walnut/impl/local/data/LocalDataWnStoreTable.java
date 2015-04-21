package org.nutz.walnut.impl.local.data;

import org.nutz.lang.Each;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnStoreTable;

public class LocalDataWnStoreTable implements WnStoreTable {

    @Override
    public int eachHistory(WnObj o, long nano, Each<WnHistory> callback) {
        WnHistory his = getHistory(o, nano);
        callback.invoke(0, his, 1);
        return 1;
    }

    @Override
    public WnHistory getHistory(WnObj o, long nano) {
        return new LocalObjWnHistory(o);
    }

    @Override
    public WnHistory addHistory(String oid, String data, String sha1, long len) {
        return null;
    }

    @Override
    public int cleanHistory(WnObj o, long nano) {
        return 0;
    }

    @Override
    public int cleanHistoryBy(WnObj o, int remain) {
        return 0;
    }

    @Override
    public void _clean_for_unit_test() {}

}
