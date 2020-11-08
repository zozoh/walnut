package org.nutz.walnut.ext.truck;

import java.util.List;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIoIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.impl.box.JvmFilterContext;

public class TruckContext extends JvmFilterContext {

    public TruckMode mode;

    public WnObj fromDir;

    public WnIoIndexer fromIndexer;

    public WnIoBM fromBM;

    public NutMap match;

    public int limit;

    public int skip;

    public NutMap sort;

    public WnObj toDir;

    public WnIoIndexer toIndexer;

    public WnIoBM toBM;

    public List<WnObj> list;

    /**
     * 桶转移时的缓冲大小，默认 8192 bytes
     */
    public int bufferSize;

    public TruckContext() {
        bufferSize = 8192;
    }

    public WnQuery getQuery() {
        WnQuery q = new WnQuery();
        if (null != match && !match.isEmpty()) {
            q.setAll(match);
        }
        if (limit > 0) {
            q.limit(limit);
        }
        if (skip > 0) {
            q.skip(skip);
        }
        if (null != sort && !sort.isEmpty()) {
            q.sort(sort);
        }
        return q;
    }

    public void appendObj(WnObj obj) {
        if (null != list) {
            list.add(obj);
        }
    }

}
