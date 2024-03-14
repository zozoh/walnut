package com.site0.walnut.ext.data.o.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.ZParams;

public class o_summary extends OFilter {
    
    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        fc.quiet = true;
        JsonFormat jfmt = Cmds.gen_json_format(params);
        String json = Json.toJson(fc.summary, jfmt);
        sys.out.println(json);
    }

}
