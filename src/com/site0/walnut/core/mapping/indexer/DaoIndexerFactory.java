package com.site0.walnut.core.mapping.indexer;

import java.util.Map;

import org.nutz.ioc.Ioc;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthService;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.indexer.dao.DaoIndexer;
import com.site0.walnut.core.mapping.WnIndexerFactory;
import com.site0.walnut.ext.sys.sql.WnDaoMappingConfig;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;

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
        // 防守一下
        if (null == oHome) {
            throw Er.create("e.io.ixf.dao.NilHome");
        }
        if (Strings.isBlank(str)) {
            throw Er.create("e.io.ixf.dao.BlankStr");
        }

        // 预备 ...
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
        if (null == me
            || null == auth
            || wc.isSecurityNoCheck()
            || auth.isMemberOfGroup(me, "root")) {
            di = indexers.get(str);
        }
        // 尝试从自己的域读取
        if (null == di) {
            String homePath;
            if (null != me) {
                homePath = me.getHomePath();
            } else {
                homePath = Wn.appendPath("/" + oHome.d0(), oHome.d1());
            }
            String fnm = str + ".json";
            String aph = Wn.appendPath(homePath, ".io/ix", fnm);
            WnObj o = io.fetch(null, aph);
            if (null != o) {
                WnDaoMappingConfig conf = WnDaos.loadConfig(WnDaoMappingConfig.class,
                                                     io,
                                                     o,
                                                     Wlang.map("HOME", homePath));
                return new DaoIndexer(oHome, mimes, conf);
            }
        }
        // 抛错
        if (null == di) {
            throw Er.create("e.io.ixf.dao.NilIndexer", str);
        }
        return di;
    }

}
