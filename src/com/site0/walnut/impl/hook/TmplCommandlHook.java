package com.site0.walnut.impl.hook;

import com.site0.walnut.util.Ws;
import com.site0.walnut.util.tmpl.WnTmpl;
import com.site0.walnut.api.hook.WnHookContext;
import com.site0.walnut.api.io.WnObj;

public class TmplCommandlHook extends AbstractWnHook {

    private WnTmpl tmpl;

    @Override
    public String getType() {
        return "tmpl";
    }

    @Override
    protected void _init(String text) {
        tmpl = Ws.isBlank(text) ? null : WnTmpl.parse(text);
    }

    @Override
    public void invoke(WnHookContext hc, WnObj o) {
        if (null != tmpl) {
            String cmdText = tmpl.render(o, false).toString();
            hc.exec(cmdText);
        }
    }

}
