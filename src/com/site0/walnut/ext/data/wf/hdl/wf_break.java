package com.site0.walnut.ext.data.wf.hdl;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.data.wf.WfContext;
import com.site0.walnut.ext.data.wf.WfFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

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
