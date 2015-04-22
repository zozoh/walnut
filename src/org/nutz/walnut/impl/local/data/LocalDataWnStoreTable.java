package org.nutz.walnut.impl.local.data;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.AbstractWnStoreTable;

public class LocalDataWnStoreTable extends AbstractWnStoreTable {

    @Override
    public int eachHistory(WnObj o, long nano, Each<WnHistory> callback) {
        WnHistory his = getHistory(o, nano);
        if (null != his) {
            callback.invoke(0, his, 1);
            return 1;
        }
        return 0;
    }

    @Override
    public WnHistory getHistory(WnObj o, long nano) {
        if (nano < 0 || nano >= o.nanoStamp()) {
            if (o.hasData() && o.hasSha1())
                return new LocalObjWnHistory(o);
        }
        return null;
    }

    @Override
    public WnHistory addHistory(String oid, String data, String sha1, long len) {
        throw Lang.noImplement();
    }

    @Override
    public List<WnHistory> cleanHistory(WnObj o, long nano) {
        List<WnHistory> list = new ArrayList<WnHistory>(1);
        if (nano < 0 || nano >= o.nanoStamp())
            list.add(new LocalObjWnHistory(o));
        return list;
    }

    @Override
    public List<WnHistory> cleanHistoryBy(WnObj o, int remain) {
        List<WnHistory> list = new ArrayList<WnHistory>(1);
        if (remain <= 0)
            list.add(new LocalObjWnHistory(o));
        return list;
    }

    @Override
    public void _clean_for_unit_test() {}

}
