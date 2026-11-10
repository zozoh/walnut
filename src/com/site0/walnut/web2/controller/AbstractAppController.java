package com.site0.walnut.web2.controller;

import java.io.InputStream;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.mvc.AbstractWnController;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcResponse;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.web.bean.WnApp;
import com.site0.walnut.web2.controller.app.WnAppRespOpsWrapper;
import com.site0.walnut.web2.controller.app.WnHttpRespStatusSetter;

public abstract class AbstractAppController extends AbstractWnController {

    @Override
    protected WnObj _find_app_home(WnMvcContext ctx, String appName) {
        String appPaths = Wn.WC().checkSession().getEnv().getString("APP_PATH");
        for (String base : Strings.splitIgnoreBlank(appPaths, ":")) {
            WnObj obj = io(ctx).fetch(null, Wn.appendPath(base, appName));
            if (null != obj)
                return obj;
        }
        return null;
    }

    @Override
    protected WnObj _check_app_home(WnMvcContext ctx, String appName) {
        WnObj home = _find_app_home(ctx, appName);
        if (null == home)
            throw Er.create("e.app.noexists", appName);
        return home;
    }

    protected WnMvcView run(WnMvcContext ctx, NutMap params) {
        String appName = arg(ctx, 0);
        String mimeType = params.getString("mime");
        String metaOutputSeparator = params.getString("mos");
        String PWD = params.getString("PWD");
        String cmdText = params.getString("cmd");
        String in = params.getString("in");
        boolean forceFlushBuffer = bool(params, "ffb");
        boolean logOff = bool(params, "log_off");
        WnMvcResponse resp = ctx.response();
        cors(ctx);
        if (options(ctx))
            return WnMvcView.none();

        if (logOff)
            Wn.WC().setLogOff();
        WnApp app = services(ctx).getAppService().checkApp(appName, auth(ctx));
        if (Strings.isBlank(mimeType))
            mimeType = "text/plain";
        resp.contentType(mimeType);

        WnHttpRespStatusSetter statusSetter = new WnHttpRespStatusSetter(resp);
        WnAppRespOpsWrapper out = new WnAppRespOpsWrapper(statusSetter, 200);
        WnAppRespOpsWrapper err = new WnAppRespOpsWrapper(statusSetter, 500);
        InputStream ins = Strings.isEmpty(in) ? null : Wlang.ins(in);
        if (forceFlushBuffer) {
            resp.header("X-ACCEL-BUFFERING", "yes");
            out.setForceFlush(true);
            err.setForceFlush(true);
        }
        services(ctx).getAppService().runCommand(app, metaOutputSeparator, PWD, cmdText, out, err, ins);
        return WnMvcView.none();
    }
}
