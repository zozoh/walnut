package com.site0.walnut.util.explain;

import org.nutz.castor.Castors;
import org.nutz.lang.util.NutBean;
import org.nutz.mapl.Mapl;

import com.site0.walnut.util.tmpl.WnTmpl;

public class WnExplainTmpl implements WnExplain {

    private String testKey;

    private WnTmpl tmpl;

    public WnExplainTmpl(String input) {
        this(input, null);
    }

    public WnExplainTmpl(String input, String test) {
        tmpl = WnTmpl.parse(input);
        testKey = test;
    }

    @Override
    public Object explain(NutBean context) {
        if (null != testKey) {
            Object tv = Mapl.cell(context, testKey);
            if (null == tv || !Castors.me().castTo(tv, Boolean.class)) {
                return null;
            }
        }
        return tmpl.render(context);
    }

}
