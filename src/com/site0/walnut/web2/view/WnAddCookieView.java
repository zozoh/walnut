package com.site0.walnut.web2.view;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.web.WnConfig;

public class WnAddCookieView implements WnMvcView {

    private final WnConfig conf;
    private final WnMvcView view;
    private final Object data;

    public WnAddCookieView(WnConfig conf, WnMvcView view, Object data) {
        this.conf = conf;
        this.view = view;
        this.data = data;
    }

    @Override
    public void render(WnMvcContext ctx) {
        if (null != data && !(data instanceof Throwable)) {
            NutMap vars = Wlang.obj2nutmap(data);
            String cookie = conf.getCookieTmpl(false).render(vars, false);
            ctx.response().addHeader("SET-COOKIE", cookie + "; Path=/;");
        }
        if (null != view)
            view.render(ctx);
    }
}
