package com.site0.walnut.web2.controller;

import java.nio.charset.StandardCharsets;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.mvc.AbstractWnController;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcRequest;
import com.site0.walnut.mvc.WnMvcResponse;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web.util.WnWeb;

public class SwitchSessionController extends AbstractWnController {

    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        WnMvcRequest req = ctx.request();
        WnMvcResponse resp = ctx.response();
        WnWeb.setCrossDomainHeaders("*", (name, value) -> {
            resp.header(WnWeb.niceHeaderName(name), value);
        });

        NutMap params = req.queryStringMap();
        String ticket = params.getString("seid");
        Object exit = params.get("exit");
        String exitValue = exit instanceof String[] ? ((String[]) exit)[0] : params.getString("exit");
        boolean isExit = !Ws.isBlank(exitValue) && Wlang.parseBoolean(exitValue);
        String oldTicket = params.getString("old_seid");
        // site/host were accepted but unused by ajax_change_session.
        WnLoginApi auth = ctx.services().getLoginApi();
        NutMap data;
        try {
            String myTicket = Wn.WC().getTicket();
            WnSession se = null;
            if (!Ws.isBlank(myTicket)) {
                se = auth.getSession(myTicket);
            }
            if (null != se && se.isSameTicket(ticket)) {
                throw Er.create("e.web.u.chse.self");
            }

            WnSession seNew = auth.checkSession(ticket);
            if (isExit) {
                if (Ws.isBlank(oldTicket)) {
                    throw Er.create("e.web.u.chse.exit.NeedOldSEID");
                }
                if (!seNew.hasChildTicket()) {
                    throw Er.create("e.web.u.chse.exit.NewSEWithoutChildTicket");
                }
                if (!seNew.getChildTicket().equals(oldTicket)) {
                    throw Er.create("e.web.u.chse.exit.NewSEChildTicketNoMatched");
                }
            }
            else {
                if (null == se) {
                    throw Er.create("e.web.u.chse.NoSession");
                }
                if (!se.isParentOf(seNew)) {
                    throw Er.create("e.web.u.chse.NotMyChild");
                }
            }
            data = seNew.toBean(auth);
        }
        catch (RuntimeException e) {
            String code = Er.wrap(e).getKey();
            if (null == code || (!code.startsWith("e.web.u.chse.")
                                 && !"e.auth.session.NoExists".equals(code))) {
                throw e;
            }
            Wlog.getMAIN().warnf("SwitchSessionController: request rejected: %s", code);
            // Legacy @Fail("ajax"): business failures keep HTTP 200 and the reason.
            return __ajax(ctx, false, code, Er.wrap(e).getReason());
        }

        // Legacy getProtocol() is HTTP/1.x, not the scheme; retain Auto semantics.
        String cookie = ctx.services().getConfig().getCookieTmpl(false).render(data, false);
        resp.addHeader("SET-COOKIE", cookie + "; Path=/;");
        return __ajax(ctx, true, null, data);
    }

    private WnMvcView __ajax(WnMvcContext ctx, boolean ok, String code, Object data) {
        NutMap body = new NutMap().setv("ok", ok).setv("errCode", code)
                                 .setv("msg", code).setv("data", data);
        // Match legacy AjaxView/Mvcs.write, including cache and JSON formatting.
        byte[] bytes = Json.toJson(body, JsonFormat.nice()).getBytes(StandardCharsets.UTF_8);
        ctx.response().header("Cache-Control", "no-cache");
        ctx.response().header("Content-Length", Integer.toString(bytes.length));
        return WnMvcView.raw(bytes, "text/json");
    }

}
