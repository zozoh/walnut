package org.nutz.walnut.core.mapping.bm;

import java.util.Map;

import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.core.bm.redis.RedisBM;
import org.nutz.walnut.core.mapping.WnBMFactory;
import org.nutz.walnut.ext.redis.Wedis;
import org.nutz.walnut.ext.redis.WedisConfig;
import org.nutz.walnut.util.Wn;

/**
 * 应对频繁读写小文件的 Redis桶管理器实现工厂类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class RedisBMFactory implements WnBMFactory {

    private WnIo io;

    private Map<String, RedisBM> bms;

    public RedisBMFactory() {}

    public void setIo(WnIo io) {
        this.io = io;
    }

    public void setBms(Map<String, RedisBM> bms) {
        this.bms = bms;
    }

    @Override
    public WnIoBM load(WnObj oHome, String str) {
        RedisBM bm = bms.get(str);
        // 尝试从自己的域读取
        if (null == bm) {
            WnAccount me = Wn.WC().getMe();
            if (null != me) {
                String aph = Wn.appendPath(me.getHomePath(), ".io/bm", str);
                WnObj o = io.fetch(null, aph);
                if (null != o) {
                    WedisConfig conf = Wedis.loadConfig(io, o);
                    return new RedisBM(conf);
                }
            }
        }
        // 抛错
        if (null == bm) {
            throw Er.create("e.io.bm.UndefinedRedisBM", str);
        }
        return bm;
    }

}
