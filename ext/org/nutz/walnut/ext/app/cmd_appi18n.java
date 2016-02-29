package org.nutz.walnut.ext.app;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_appi18n extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);

        String lang = "zh-cn";
        if (params.vals.length > 0)
            lang = params.vals[0];

        WnObj o = Wn.checkObj(sys, "~/.ui/i18n/" + lang + ".js");
        NutMap json = sys.io.readJson(o, NutMap.class);

        JsonFormat fmt = this.gen_json_format(params);
        sys.out.println(Json.toJson(json, fmt));

    }

}
