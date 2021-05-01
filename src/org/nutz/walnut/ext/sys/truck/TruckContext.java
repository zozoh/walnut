package org.nutz.walnut.ext.sys.truck;

import java.util.List;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnIoIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.ext.sys.truck.impl.TruckPrinter;
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

    public WnIo io;
    
    /**
     * 只有不存在的，才插入（会预先检查，所以慢一些）
     */
    public boolean noexists;
    
    /**
     * 新创建的数据，需要重新生成 ID
     */
    public boolean genId;

    public List<WnObj> list;

    public TruckPrinter printer;

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

}
