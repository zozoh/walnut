package org.nutz.walnut.ext.data.sqlx.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.ext.data.sqlx.SqlxContext;
import org.nutz.walnut.ext.data.sqlx.SqlxFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

public class sqlx_json extends SqlxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        JsonFormat jfmt = Cmds.gen_json_format(params);
        String str = Json.toJson(fc.result, jfmt);
        sys.out.println(str);
    }

}
