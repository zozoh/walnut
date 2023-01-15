package org.nutz.walnut.ext.util.react.hdl;

import org.nutz.walnut.util.tmpl.WnTmpl;
import org.nutz.json.Json;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.util.react.ReactContext;
import org.nutz.walnut.ext.util.react.ReactFilter;
import org.nutz.walnut.ext.util.react.bean.ReactItem;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class react_config extends ReactFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(clear)$");
    }

    @Override
    protected void process(WnSystem sys, ReactContext fc, ZParams params) {
        for (String v : params.vals) {
            String ph = WnTmpl.exec(v, fc.vars);
            WnObj oConfig = Wn.getObj(sys, ph);
            if (null == oConfig) {
                continue;
            }
            ReactItem[] items;
            String json = sys.io.readText(oConfig);
            if (Ws.isBlank(json)) {
                items = new ReactItem[0];
            } else {
                items = Json.fromJson(ReactItem[].class, json);
            }

            fc.config.addItems(items);
        }

    }

}
