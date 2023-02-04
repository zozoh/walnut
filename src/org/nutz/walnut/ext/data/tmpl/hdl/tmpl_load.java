package org.nutz.walnut.ext.data.tmpl.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.tmpl.TmplContext;
import org.nutz.walnut.ext.data.tmpl.TmplFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class tmpl_load extends TmplFilter {

    @Override
    protected void process(WnSystem sys, TmplContext fc, ZParams params) {

        // 从文件里获得
        if (params.hasString("f")) {
            String ph = params.getString("f");
            WnObj o = Wn.checkObj(sys, ph);
            fc.tmpl = sys.io.readText(o);
        }
        // 从参数里获得
        else if (params.vals.length > 0) {
            fc.tmpl = params.val(0);
        }
        // 从管道获得
        else {
            fc.tmpl = sys.in.readAll();
        }
    }

}
