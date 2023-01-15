package org.nutz.walnut.impl.hook;

import org.nutz.lang.Strings;
import org.nutz.walnut.util.tmpl.WnTmpl;
import org.nutz.walnut.api.hook.WnHookContext;
import org.nutz.walnut.api.io.WnObj;

public class TmplCommandlHook extends AbstractWnHook {

    private WnTmpl tmpl;

    @Override
    public String getType() {
        return "tmpl";
    }

    @Override
    protected void _init(String text) {
        tmpl = Strings.isBlank(text) ? null : WnTmpl.parse(text);
    }

    @Override
    public void invoke(WnHookContext hc, WnObj o) {
        if (null != tmpl) {
            String cmdText = tmpl.render(o, false).toString();
            hc.exec(cmdText);
        }
    }

}
