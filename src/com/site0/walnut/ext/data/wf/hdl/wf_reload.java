package com.site0.walnut.ext.data.wf.hdl;

import com.site0.walnut.ext.data.wf.WfContext;
import com.site0.walnut.ext.data.wf.WfFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class wf_reload extends WfFilter {

    @Override
    protected void process(WnSystem sys, WfContext fc, ZParams params) {
        fc.reloadVars();
    }

}
