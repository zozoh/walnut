package org.nutz.walnut.ext.data.o.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

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
