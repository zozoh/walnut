package com.site0.walnut.ext.util.react.hdl;

import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.json.Json;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.util.react.ReactContext;
import com.site0.walnut.ext.util.react.ReactFilter;
import com.site0.walnut.ext.util.react.bean.ReactItem;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

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
