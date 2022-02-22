package org.nutz.walnut.ext.data.o.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wobj;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

public class o_ajax extends o_json {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnl", "^(path)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        JsonFormat jfmt = Cmds.gen_json_format(params);

        fc.keepAsList = fc.params.is("l", fc.keepAsList);

        String keys = params.val(0);
        WnMatch ma = Wobj.explainObjKeyMatcher(keys);

        Object data = fc.toOutput(ma, params.is("path"));

        AjaxReturn re = Ajax.ok().setData(data);
        String json = Json.toJson(re, jfmt);
        sys.out.println(json);
        fc.quiet = true;
    }

}
