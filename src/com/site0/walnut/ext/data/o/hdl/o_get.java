package com.site0.walnut.ext.data.o.hdl;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class o_get extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(ignore|fallback|reset)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        boolean ignore = params.is("ignore");
        boolean fallback = params.is("fallback");
        if (params.is("reset")) {
            fc.clearAll();
        }
        for (String val : params.vals) {
            String[] ids = Ws.splitIgnoreBlank(val);
            for (String id : ids) {
                // fallback 模式
                if (fallback) {
                    WnObj o = sys.io.get(id);
                    if (null == o) {
                        continue;
                    }
                    fc.add(o);
                    break;
                }
                // 没有就忽略
                else if (ignore) {
                    WnObj o = sys.io.get(id);
                    if (null != o) {
                        fc.add(o);
                    }
                    break;
                }
                // 必须存在
                WnObj o = sys.io.checkById(id);
                fc.add(o);
            }
        }
    }

}
