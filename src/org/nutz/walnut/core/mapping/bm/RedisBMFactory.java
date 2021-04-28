package org.nutz.walnut.core.mapping.bm;

import java.util.Map;

import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.core.bm.redis.RedisBM;
import org.nutz.walnut.core.mapping.WnBMFactory;
import org.nutz.walnut.ext.sys.redis.Wedis;
import org.nutz.walnut.ext.sys.redis.WedisConfig;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

/**
 * 应对频繁读写小文件的 Redis桶管理器实现工厂类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class RedisBMFactory implements WnBMFactory {

    private WnAuthService auth;

    private WnIo io;

    private Map<String, RedisBM> bms;

    public void setAuth(WnAuthService auth) {
        this.auth = auth;
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public void setBms(Map<String, RedisBM> bms) {
        this.bms = bms;
    }

    @Override
    public WnIoBM load(WnObj oHome, String str) {
        RedisBM bm = null;
        WnContext wc = Wn.WC();
        WnAccount me = wc.getMe();
        // 如果当前用户为 root 组成员，或者当前木有做权限检查，可以从预定义里获取
        if (null == auth || wc.isSecurityNoCheck() || auth.isMemberOfGroup(me, "root")) {
            bm = bms.get(str);
        }
        // 尝试从自己的域读取
        if (null == bm && null != me) {
            String fnm = str + ".json";
            String aph = Wn.appendPath(me.getHomePath(), ".io/bm", fnm);
            WnObj o = io.fetch(null, aph);
            if (null != o) {
                WedisConfig conf = Wedis.loadConfig(io, o);
                return new RedisBM(conf);
            }
        }
        // 抛错
        if (null == bm) {
            throw Er.create("e.io.bm.UndefinedRedisBM", str);
        }
        return bm;
    }

}
