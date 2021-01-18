package org.nutz.walnut.ext.o.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.ext.o.OContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

public class o_ajax extends o_json {

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        JsonFormat jfmt = Cmds.gen_json_format(params);
        Object data = fc.toOutput();
        AjaxReturn re = Ajax.ok().setData(data);
        String json = Json.toJson(re, jfmt);
        sys.out.println(json);
        fc.alreadyOutputed = true;
    }

}
