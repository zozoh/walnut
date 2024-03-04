package org.nutz.walnut.ext.data.tmpl.hdl;

import org.nutz.walnut.ext.data.tmpl.TmplContext;
import org.nutz.walnut.ext.data.tmpl.TmplFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.tmpl.WnTmplX;

public class tmpl_render extends TmplFilter {

    @Override
    protected void process(WnSystem sys, TmplContext fc, ZParams params) {
        WnTmplX t = WnTmplX.parse(null, fc.expert, fc.tmpl);
        String s = t.render(fc.vars, fc.showKeys);
        fc.quiet = true;
        sys.out.println(s);
    }

}
