package com.site0.walnut.ext.data.expl.hdl;

import com.site0.walnut.ext.data.expl.ExplContext;
import com.site0.walnut.ext.data.expl.ExplFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class expl_render extends ExplFilter {

    @Override
    protected void process(WnSystem sys, ExplContext fc, ZParams params) {
        fc.quiet = true;
        String re = fc.renderToStr();
        sys.out.print(re);
    }

}
