package org.nutz.walnut.core.mapping.indexer;

import java.util.Map;

import org.nutz.ioc.Ioc;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoIndexer;
import org.nutz.walnut.core.indexer.dao.DaoIndexer;
import org.nutz.walnut.core.mapping.WnIndexerFactory;
import org.nutz.walnut.ext.sql.WnDaoConfig;
import org.nutz.walnut.ext.sql.WnDaos;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public class DaoIndexerFactory implements WnIndexerFactory {

    private Ioc ioc;

    private String authServiceName;

    private WnAuthService auth;

    private WnIo io;

    private Map<String, DaoIndexer> indexers;

    private MimeMap mimes;

    public void setAuth(WnAuthService auth) {
        this.auth = auth;
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public void setIndexers(Map<String, DaoIndexer> indexers) {
        this.indexers = indexers;
    }

    public void setMimes(MimeMap mimes) {
        this.mimes = mimes;
    }

    @Override
    public WnIoIndexer load(WnObj oHome, String str) {
        DaoIndexer di = null;
        WnContext wc = Wn.WC();
        WnAccount me = wc.getMe();

        // 懒加载验证接口
        if (null == auth) {
            if (null != ioc && null != this.authServiceName) {
                this.auth = ioc.get(WnAuthService.class, authServiceName);
            }
        }

        // 如果当前用户为 root 组成员，或者当前木有做权限检查，可以从预定义里获取
        if (null == auth || wc.isSecurityNoCheck() || auth.isMemberOfGroup(me, "root")) {
            di = indexers.get(str);
        }
        // 尝试从自己的域读取
        if (null == di && null != me) {
            String fnm = str + ".json";
            String aph = Wn.appendPath(me.getHomePath(), ".io/ix", fnm);
            WnObj o = io.fetch(null, aph);
            if (null != o) {
                WnDaoConfig conf = WnDaos.loadConfig(io, o, Lang.map("HOME", me.getHomePath()));
                return new DaoIndexer(oHome, mimes, conf);
            }
        }
        // 抛错
        if (null == di) {
            throw Er.create("e.io.bm.UndefinedRedisBM", str);
        }
        return di;
    }

}
