package com.site0.walnut.web2.controller;

import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web2.view.WnAddCookieView;
import com.site0.walnut.web2.view.WnLoginPage;

public class AppLoginPageController extends AbstractAppController {
    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        // 2026-11-10 路由合并：/a/login 与 /a/login/** 并成一条 /a/login/**；
        // 裸 /a/login 补 302 → /a/login/（URL 规范化，原 AppLoginController 的行为）
        String path = ctx.request().getPath();
        if (Ws.isBlank(arg(ctx, 0)) && !path.endsWith("/"))
            return WnMvcView.redirect(path + "/");

        WnSession se = auth(ctx).getSession(Wn.WC().getTicket());
        if (null != se && !se.isExpired()) {
            String app = se.getEnv().getString("OPEN", "wn-term");
            return new WnAddCookieView(conf(ctx), WnMvcView.redirect("/a/open/" + app), se.toBean(auth(ctx)));
        }
        String domain = attr(ctx, "wn_www_grp");
        WnUser domainUser = null == domain ? null : auth(ctx).getUser(domain);
        WnLoginPage login = new WnLoginPage();
        login.setIo(io(ctx));
        login.setConfig(conf(ctx).toBean());
        login.setPageName(params(ctx.request()).getString("pg"));
        login.setDomainUser(domainUser);
        login.setHost(attr(ctx, "wn_www_host"));
        login.setSitePath(attr(ctx, "wn_www_site"));
        login.setEtag(ctx.request().getHeader("If-None-Match"));
        login.setRange(ctx.request().getHeader("Range"));
        return login.genView(arg(ctx, 0));
    }
}
