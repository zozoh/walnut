package com.site0.walnut.ext.data.sqlx.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.ZParams;

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
        
        fc.quiet = true;
    }

}
