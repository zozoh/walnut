package com.site0.walnut.ext.data.expl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.impl.box.JvmFilterContext;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.explain.WnExplain;
import com.site0.walnut.util.explain.WnExplains;

public class ExplContext extends JvmFilterContext {

    public NutMap vars;

    public String input;

    public boolean quiet;

    public boolean showKeys;

    public WnExplain getRender() {
        if (Ws.isBlank(input)) {
            return null;
        }
        String str = Ws.trim(input);
        Object src;
        if (Ws.isQuoteBy(str, '[', ']') || Ws.isQuoteBy(str, '{', '}')) {
            src = Json.fromJson(str);
        } else {
            src = str;
        }
        return WnExplains.parse(src);
    }

    public Object render() {
        WnExplain expl = this.getRender();
        if (null == expl) {
            return null;
        }
        NutMap ctx = vars;
        if (null == ctx) {
            ctx = new NutMap();
        }
        return expl.explain(ctx);
    }

    public String renderToStr() {
        Object re = render();
        if (null != re) {
            return Json.toJson(re);
        }
        return null;
    }
}
