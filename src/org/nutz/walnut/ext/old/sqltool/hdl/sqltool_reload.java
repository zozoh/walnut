package org.nutz.walnut.ext.old.sqltool.hdl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.ext.old.sqltool.SqlToolHelper;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class sqltool_reload implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        WnAccount me = sys.getMe();
        // 获取Dao对象
        String dsName = hc.oRefer.name();
        NutMap dsConf = hc.getAs("dataSource_conf", NutMap.class);
        SqlToolHelper.reload(me, dsName, dsConf);
        sys.out.println("{ok:true}");
    }

}