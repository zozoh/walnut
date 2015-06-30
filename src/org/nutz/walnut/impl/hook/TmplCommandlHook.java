package org.nutz.walnut.impl.hook;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.Context;
import org.nutz.walnut.api.hook.WnHookContext;
import org.nutz.walnut.api.io.WnObj;

public class TmplCommandlHook extends AbstractWnHook {

    private Segment seg;

    @Override
    protected void _init(String text) {
        seg = Strings.isBlank(text) ? null : Segments.create(text);
    }

    @Override
    public void invoke(WnHookContext hc, WnObj o) {
        if (null != seg) {
            Context c = Lang.context();
            c.putAll(o);
            String cmdText = seg.render(c).toString();
            hc.exec(cmdText);
        }
    }

}
