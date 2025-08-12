package com.site0.walnut.ext.data.sqlx.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.ZParams;

public class sqlx_pipe extends SqlxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(view)$");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        boolean viewMode = params.is("view");

        // 如果是查看模式
        if (viewMode) {
            fc.quiet = true;
            JsonFormat jfmt = Cmds.gen_json_format(params);
            sys.out.println(Json.toJson(fc.getPipeContext(), jfmt));
        }
        // 那就是设置模式
        else {
            for (String str : params.vals) {
                NutMap json = Wlang.map(str);
                fc.putAllPipeContext(json);
            }
        }

    }

}
