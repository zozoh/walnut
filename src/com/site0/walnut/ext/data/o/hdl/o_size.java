package com.site0.walnut.ext.data.o.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class o_size extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        String key = params.val(0);
        int n = fc.list.size();
        fc.quiet = true;

        if (Ws.isBlank(key)) {
            sys.out.printf("%s", n);
        }
        // 采用JSON输出
        else {
            JsonFormat jfmt = Cmds.gen_json_format(params);
            Object re = Wlang.map(key, n);
            sys.out.println(Json.toJson(re, jfmt));
        }
    }

}
