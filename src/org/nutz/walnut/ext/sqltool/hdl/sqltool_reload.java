package org.nutz.walnut.ext.sqltool.hdl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.sqltool.SqlToolHelper;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class sqltool_reload implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 获取Dao对象
        String dsName = hc.oRefer.name();
        NutMap dsConf = hc.getAs("dataSource_conf", NutMap.class);
        SqlToolHelper.reload(sys.me, dsName, dsConf);
        sys.out.println("{ok:true}");
    }

}
