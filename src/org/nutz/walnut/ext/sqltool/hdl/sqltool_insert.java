package org.nutz.walnut.ext.sqltool.hdl;

import java.util.List;

import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.sqltool.SqlToolHelper;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class sqltool_insert implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 获取Dao对象
        String dsName = hc.oRefer.name();
        NutMap dsConf = hc.getAs("dataSource_conf", NutMap.class);
        Dao dao = SqlToolHelper.getDao(sys.me, dsName, dsConf);

        // 开始Record模式的插入
        String tableName = hc.params.val(0);
        List<NutMap> params;
        if (hc.params.has("params")) {
            System.out.println(hc.params.get("params"));
            params = Json.fromJsonAsList(NutMap.class, hc.params.check("params").trim());
        } else {
            params = Json.fromJsonAsList(NutMap.class, sys.in.getReader());
        }
        if (params.isEmpty()) {
            return;
        }
        params.get(0).setv(".table", tableName);
        params = dao.insert(params);
        sys.out.writeJson(params, JsonFormat.full());
    }

}
