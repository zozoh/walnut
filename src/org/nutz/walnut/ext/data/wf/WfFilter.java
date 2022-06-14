package org.nutz.walnut.ext.data.wf;

import org.nutz.json.Json;
import org.nutz.walnut.impl.box.JvmFilter;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

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
