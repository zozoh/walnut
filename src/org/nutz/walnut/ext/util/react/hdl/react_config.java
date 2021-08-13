package org.nutz.walnut.ext.util.react.hdl;

import org.nutz.lang.tmpl.Tmpl;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.util.react.ReactContext;
import org.nutz.walnut.ext.util.react.ReactFilter;
import org.nutz.walnut.ext.util.react.bean.ReactItem;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class react_config extends ReactFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(clear)$");
    }

    @Override
    protected void process(WnSystem sys, ReactContext fc, ZParams params) {
        for (String v : params.vals) {
            String ph = Tmpl.exec(v, fc.vars);
            WnObj oConfig = Wn.getObj(sys, ph);
            if (null == oConfig) {
                continue;
            }
            ReactItem[] items = sys.io.readJson(oConfig, ReactItem[].class);
            fc.config.addItems(items);
        }

    }

}
