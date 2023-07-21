package org.nutz.walnut.ext.data.wf.hdl;

import org.nutz.walnut.ext.data.wf.WfContext;
import org.nutz.walnut.ext.data.wf.WfFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class wf_reload extends WfFilter {

    @Override
    protected void process(WnSystem sys, WfContext fc, ZParams params) {
        fc.reloadVars();
    }

}
