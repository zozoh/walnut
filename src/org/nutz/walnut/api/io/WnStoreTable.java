package org.nutz.walnut.api.io;

import org.nutz.lang.Each;
import org.nutz.walnut.util.UnitTestable;

public interface WnStoreTable extends UnitTestable {

    int eachHistory(WnObj o, long nano, Each<WnHistory> callback);

    WnHistory getHistory(WnObj o, long nano);

    WnHistory addHistory(String oid, String data, String sha1, long len);

    int cleanHistory(WnObj o, long nano);

    int cleanHistoryBy(WnObj o, int remain);

}
