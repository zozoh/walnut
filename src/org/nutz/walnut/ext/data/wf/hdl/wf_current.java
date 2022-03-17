package org.nutz.walnut.ext.data.wf.hdl;

import org.nutz.walnut.ext.data.wf.WfContext;
import org.nutz.walnut.ext.data.wf.WfFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class wf_current extends WfFilter {

    @Override
    protected void process(WnSystem sys, WfContext fc, ZParams params) {
        for (String v : params.vals) {
            Object re = Wn.explainObj(fc.vars, v);
            if (null != re) {
                fc.setCurrentName(re.toString());
                break;
            }
        }
    }

}
