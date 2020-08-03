package org.nutz.walnut.core.bm.redis;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleManager;
import org.nutz.walnut.core.WnIoIndexer;
import org.nutz.walnut.core.bm.AbstractIoBM;
import org.nutz.walnut.core.bm.localfile.LocalFileReadHandle;
import org.nutz.walnut.core.bm.localfile.LocalFileReadWriteHandle;
import org.nutz.walnut.core.bm.localfile.LocalFileWriteHandle;
import org.nutz.walnut.ext.redis.WedisConfig;
import org.nutz.walnut.util.Wn;

public class RedisIoBM extends AbstractIoBM {

    private WedisConfig conf;

    public RedisIoBM(WnIoHandleManager handles, WedisConfig conf) {
        super(handles);
        this.conf = conf;
    }

    @Override
    public boolean isSame(WnIoBM bm) {
        if (bm instanceof RedisIoBM) {
            RedisIoBM rdbm = (RedisIoBM) bm;
            return rdbm.conf.equals(this.conf);
        }
        return false;
    }

    @Override
    public WnIoHandle createHandle(int mode) {
        // 只读
        if (Wn.S.canRead(mode)) {
            return new RedisReadWriteHandle(this);
        }
        // 只写
        if (Wn.S.isWrite(mode)) {
            return new RedisReadWriteHandle(this);
        }
        // 追加
        if (Wn.S.isAppend(mode)) {
            return new RedisReadWriteHandle(this, true);
        }
        // 修改
        if (Wn.S.canModify(mode) || Wn.S.isReadWrite(mode)) {
            return new RedisReadWriteHandle(this, true);
        }
        throw Er.create("e.io.bm.localfile.NonsupportMode", mode);
    }

    @Override
    public long copy(String buckId, String referId) {
        return 0;
    }

    @Override
    public long remove(String buckId, String referId) {
        return 0;
    }

    @Override
    public long truncate(WnObj o, long len, WnIoIndexer indexer) {
        return 0;
    }

}
