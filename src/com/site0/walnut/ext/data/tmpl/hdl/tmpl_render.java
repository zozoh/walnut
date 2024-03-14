package com.site0.walnut.ext.data.tmpl.hdl;

import com.site0.walnut.ext.data.tmpl.TmplContext;
import com.site0.walnut.ext.data.tmpl.TmplFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.tmpl.WnTmplX;

public class tmpl_render extends TmplFilter {

    @Override
    protected void process(WnSystem sys, TmplContext fc, ZParams params) {
        WnTmplX t = WnTmplX.parse(null, fc.expert, fc.tmpl);
        String s = t.render(fc.vars, fc.showKeys);
        fc.quiet = true;
        sys.out.println(s);
    }

}
