package org.nutz.walnut.ext.media.lessc.hdl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.media.lessc.WnLesscService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class lessc_compile implements JvmHdl {

    protected GenericObjectPool<WnLesscService> pool;

    public lessc_compile() {
        init();
    }

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String path = Wn.normalizeFullPath(hc.params.val_check(0), sys);
        WnObj wobj = sys.io.check(null, path);
        List<WnObj> bases = new ArrayList<>();
        // 支持一下include-path试试
        if (hc.params.has("include-path")) {
            for (String _path : Strings.splitIgnoreBlank(hc.params.get("include-path"), "[; ]")) {
                bases.add(sys.io.check(null, Wn.normalizeFullPath(_path, sys)));
            }
        }
        // 支持一下pri-path试试
        List<WnObj> pris = new ArrayList<>();
        if (hc.params.has("pri-path")) {
            for (String _path : Strings.splitIgnoreBlank(hc.params.get("pri-path"), "[; ]")) {
                pris.add(sys.io.check(null, Wn.normalizeFullPath(_path, sys)));
            }
        }
        // 来吧,渲染之
        WnLesscService lessc = null;
        try {
            lessc = pool.borrowObject();
            if (lessc.getIo() == null)
                lessc.setIo(sys.io);
            sys.out.print(lessc.renderWnObj(wobj, bases, pris));
        }
        finally {
            if (lessc != null)
                pool.returnObject(lessc);
        }
    }

    public void init() {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(16);
        config.setMaxIdle(-1);
        pool = new GenericObjectPool<>(new BasePooledObjectFactory<WnLesscService>() {

            public WnLesscService create() throws Exception {
                WnLesscService lessc = new WnLesscService();
                lessc.init();
                return lessc;
            }

            public PooledObject<WnLesscService> wrap(WnLesscService lessc) {
                return new DefaultPooledObject<WnLesscService>(lessc);
            }
        }, config);
    }
}
