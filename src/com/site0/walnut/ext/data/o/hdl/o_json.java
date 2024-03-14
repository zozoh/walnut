package com.site0.walnut.ext.data.o.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wobj;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.validate.WnMatch;

public class o_json extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnl", "^path$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        JsonFormat jfmt = Cmds.gen_json_format(params);

        fc.keepAsList = params.is("l", fc.keepAsList);

        WnMatch[] mas = new WnMatch[params.vals.length];
        for (int i = 0; i < params.vals.length; i++) {
            String val = params.vals[i];
            WnMatch ma = Wobj.explainObjKeyMatcher(val);
            mas[i] = ma;
        }

        Object reo = fc.toOutput(params.is("path"), mas);

        String json = Json.toJson(reo, jfmt);
        sys.out.println(json);
        fc.quiet = true;
    }

}
