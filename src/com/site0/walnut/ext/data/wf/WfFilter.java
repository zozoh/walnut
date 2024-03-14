package com.site0.walnut.ext.data.wf;

import org.nutz.json.Json;
import com.site0.walnut.impl.box.JvmFilter;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

public abstract class WfFilter extends JvmFilter<WfContext> {

    protected boolean isCanGoingOn(WfContext fc, ZParams params) {
        boolean go_on = true;
        if (params.has("when")) {
            String when = params.getString("when");
            Object mobj;
            if (Ws.isQuoteBy(when, '[', ']') || Ws.isQuoteBy(when, '{', '}')) {
                mobj = Json.fromJson(when);
            }
            // 简写的 Map
            else {
                mobj = Wlang.map(when);
            }
            WnMatch m = AutoMatch.parse(mobj);
            go_on = m.match(fc.vars);
        }
        return go_on;
    }

}
