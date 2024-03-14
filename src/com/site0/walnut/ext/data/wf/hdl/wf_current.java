package com.site0.walnut.ext.data.wf.hdl;

import com.site0.walnut.ext.data.wf.WfContext;
import com.site0.walnut.ext.data.wf.WfFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class wf_current extends WfFilter {

    @Override
    protected void process(WnSystem sys, WfContext fc, ZParams params) {
        fc.reloadVars();
        for (String v : params.vals) {
            Object re = Wn.explainObj(fc.vars, v);
            if (null != re) {
                String currentName = re.toString();
                if (!Ws.isBlank(currentName)) {
                    fc.setCurrentName(currentName);
                    break;
                }
            }
        }
    }

}
