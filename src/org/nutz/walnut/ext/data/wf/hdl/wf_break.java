package org.nutz.walnut.ext.data.wf.hdl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.data.wf.WfContext;
import org.nutz.walnut.ext.data.wf.WfFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class wf_break extends WfFilter {

    @Override
    protected void process(WnSystem sys, WfContext fc, ZParams params) {
        // 循环判断
        for (String val : params.vals) {
            NutMap map = Wlang.map(val);
            WnMatch m = AutoMatch.parse(map);
            if (m.match(fc.vars)) {
                fc.setBreakExec(true);
                break;
            }
        }
    }

}
