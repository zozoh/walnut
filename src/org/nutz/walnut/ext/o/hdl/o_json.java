package org.nutz.walnut.ext.o.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.ext.o.OContext;
import org.nutz.walnut.ext.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wobj;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.validate.WnMatch;

public class o_json extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        JsonFormat jfmt = Cmds.gen_json_format(params);

        String keys = params.val(0);
        WnMatch ma = Wobj.explainObjKeyMatcher(keys);

        Object reo = fc.toOutput(ma);

        String json = Json.toJson(reo, jfmt);
        sys.out.println(json);
        fc.alreadyOutputed = true;
    }

}
