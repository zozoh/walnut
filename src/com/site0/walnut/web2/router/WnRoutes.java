package com.site0.walnut.web2.router;

import com.site0.walnut.mvc.WnMvcFilter;
import com.site0.walnut.mvc.WnMvcHttpMethod;
import com.site0.walnut.mvc.WnMvcMapping;
import com.site0.walnut.web2.controller.SwitchSessionController;
import com.site0.walnut.web2.controller.WebRootController;

public final class WnRoutes {

    private WnRoutes() {}

    public static void register(WnMvcMapping mapping) {
        WnMvcFilter[] none = new WnMvcFilter[0];
        mapping.route(new WnMvcHttpMethod[]{WnMvcHttpMethod.GET},
                      "/u/ajax/chse", SwitchSessionController.class, none, none);

        // JETTY-07: App routes; JETTY-08: Obj routes; JETTY-09: HttpApi routes.
        // Matching is first-registered-first: the catch-all must stay last.
        mapping.route(new WnMvcHttpMethod[]{WnMvcHttpMethod.ANY},
                      "/**", WebRootController.class, none, none);
    }

}
