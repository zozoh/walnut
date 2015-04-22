package org.nutz.walnut.api.io;

import java.util.List;

import org.nutz.lang.Each;
import org.nutz.walnut.util.UnitTestable;

public interface WnStoreTable extends UnitTestable {

    int eachHistory(WnObj o, long nano, Each<WnHistory> callback);

    List<WnHistory> getHistoryList(WnObj o, long nano);

    WnHistory getHistory(WnObj o, long nano);

    WnHistory addHistory(String oid, String data, String sha1, long len);

    List<WnHistory> cleanHistory(WnObj o, long nano);

    List<WnHistory> cleanHistoryBy(WnObj o, int remain);

}
