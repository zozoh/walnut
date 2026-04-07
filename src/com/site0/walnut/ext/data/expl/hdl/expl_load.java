package com.site0.walnut.ext.data.expl.hdl;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.expl.ExplContext;
import com.site0.walnut.ext.data.expl.ExplFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class expl_load extends ExplFilter {

    @Override
    protected void process(WnSystem sys, ExplContext fc, ZParams params) {

        // 从文件里获得
        if (params.hasString("f")) {
            String ph = params.getString("f");
            WnObj o = Wn.checkObj(sys, ph);
            fc.input = sys.io.readText(o);
        }
        // 从参数里获得
        else if (params.vals.length > 0) {
            fc.input = params.val(0);
        }
        // 从管道获得
        else {
            fc.input = sys.in.readAll();
        }
    }

}
