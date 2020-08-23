package org.nutz.walnut.core.bm.redis;

import org.nutz.lang.Encoding;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoIndexer;
import org.nutz.walnut.core.bm.AbstractIoBM;
import org.nutz.walnut.ext.redis.Wedis;
import org.nutz.walnut.ext.redis.WedisConfig;
import org.nutz.walnut.util.Wn;

public class RedisBM extends AbstractIoBM {

    private String prefix;

    private WedisConfig conf;

    public RedisBM(WedisConfig conf) {
        super(null);
        this.conf = conf;
        this.prefix = conf.setup().getString("prefix", "io:bm:");
    }

    @Override
    public boolean isSame(WnIoBM bm) {
        if (bm instanceof RedisBM) {
            RedisBM rdbm = (RedisBM) bm;
            return rdbm.conf.equals(this.conf);
        }
        return false;
    }

    @Override
    public WnIoHandle createHandle(int mode) {
        // 只读
        if (Wn.S.canRead(mode)) {
            return new RedisReadWriteHandle(this, true);
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
    public long copy(WnObj oSr, WnObj oTa) {
        // 得到源桶
        byte[] bs = this.getBytes(oSr.id());

        // 保存到目标桶
        this.setBytes(oTa.id(), bs);

        return bs.length;
    }

    @Override
    public long remove(WnObj o) {
        byte[] key = _KEY(o.id());
        Wedis.run(conf, jed -> {
            jed.del(key);
        });
        return 0;
    }

    @Override
    public long truncate(WnObj o, long len, WnIoIndexer indexer) {
        byte[] bs = this.getBytes(o.id());
        if (null != bs) {
            int n = Math.min((int) len, bs.length);
            byte[] bs2 = new byte[n];
            System.arraycopy(bs, 0, bs2, 0, n);
            this.setBytes(o.id(), bs2);

            o.lastModified(Wn.now());
            o.sha1("pending");
            o.len(n);
            indexer.set(o, "^(lm|len|sha1)$");
        }
        return -1;
    }

    public void setBytes(String buckId, byte[] bs) {
        byte[] key = _KEY(buckId);
        Wedis.run(conf, jed -> {
            jed.set(key, bs);
        });
    }

    public byte[] getBytes(String buckId) {
        byte[] key = _KEY(buckId);
        return Wedis.runGet(conf, jed -> {
            return jed.get(key);
        });
    }

    private byte[] _KEY(String buckId) {
        String str = this.prefix + buckId;
        return str.getBytes(Encoding.CHARSET_UTF8);
    }
}
