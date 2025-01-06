package com.site0.walnut.ext.data.sqlx.hdl;

import org.nutz.json.Json;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.ext.data.sqlx.hislog.SqlxHislogConfig;
import com.site0.walnut.ext.data.sqlx.hislog.SqlxHislogRuntime;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class sqlx_hislog extends SqlxFilter {

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        String logConfPath = params.check("f");
        WnObj oLogConf = Wn.checkObj(sys, logConfPath);
        String json = sys.io.readText(oLogConf);
        SqlxHislogConfig config = Json.fromJson(SqlxHislogConfig.class, json);
        fc.hislog = new SqlxHislogRuntime(config, fc);
    }

}
